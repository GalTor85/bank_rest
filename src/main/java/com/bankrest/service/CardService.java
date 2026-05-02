package com.bankrest.service;

import com.bankrest.dto.CardRequest;
import com.bankrest.dto.CardResponse;
import com.bankrest.dto.TransferRequest;
import com.bankrest.entity.Card;
import com.bankrest.entity.CardStatus;
import com.bankrest.entity.Role;
import com.bankrest.entity.User;
import com.bankrest.exception.AccessDeniedException;
import com.bankrest.exception.CardNotFoundException;
import com.bankrest.exception.InsufficientFundsException;
import com.bankrest.exception.UserNotFoundException;
import com.bankrest.repository.CardRepository;
import com.bankrest.repository.UserRepository;
import com.bankrest.util.CardMaskUtil;
import com.bankrest.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final CardMaskUtil cardMaskUtil;

    public CardResponse createCard(CardRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден: " + username));

        Card card = Card.builder()
                .encryptedNumber(encryptionUtil.encrypt(request.getCardNumber()))
                .lastFourDigits(request.getCardNumber().substring(12))
                .owner(user)
                .expiryDate(request.getExpiryDate())
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();

        card = cardRepository.save(card);
        log.info("Card created: id={}, owner={}", card.getId(), username);

        return mapToResponse(card);
    }

    public CardResponse getCard(Long cardId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена: id=" + cardId));

        if (user.getRole() != Role.ADMIN && !card.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Доступ запрещён: это не ваша карта");
        }

        return mapToResponse(card);
    }

    public Page<CardResponse> getUserCards(String username, Long ownerId, Pageable pageable) {
        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        Long targetOwnerId = requester.getRole() == Role.ADMIN && ownerId != null
                ? ownerId
                : requester.getId();

        return cardRepository.findByOwnerId(targetOwnerId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public CardResponse blockCard(Long cardId, String username) {
        Card card = findAndCheckAccess(cardId, username);
        card.setStatus(CardStatus.BLOCKED);
        log.info("Card blocked: id={}", cardId);
        return mapToResponse(cardRepository.save(card));
    }

    @Transactional
    public CardResponse activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена: id=" + cardId));
        card.setStatus(CardStatus.ACTIVE);
        log.info("Card activated: id={}", cardId);
        return mapToResponse(cardRepository.save(card));
    }

    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена: id=" + cardId));
        cardRepository.delete(card);
        log.info("Card deleted: id={}", cardId);
    }

    @Transactional
    public void transfer(TransferRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new CardNotFoundException("Карта отправителя не найдена"));

        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new CardNotFoundException("Карта получателя не найдена"));

        if (!fromCard.getOwner().getId().equals(user.getId()) || !toCard.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Обе карты должны принадлежать вам");
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Обе карты должны быть активны");
        }

        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Недостаточно средств на карте отправителя");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        log.info("Transfer: {} RUB from card {} to card {}", request.getAmount(), fromCard.getId(), toCard.getId());
    }

    private Card findAndCheckAccess(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена: id=" + cardId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (user.getRole() != Role.ADMIN && !card.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("Доступ запрещён");
        }

        return card;
    }

    private CardResponse mapToResponse(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .maskedNumber(cardMaskUtil.mask(card.getLastFourDigits()))
                .ownerUsername(card.getOwner().getUsername())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .build();
    }
}