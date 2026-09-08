package uz.insonline.travel.commons.telegram.service;


import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import uz.insonline.travel.commons.telegram.dto.TelegramRequest;

public interface TelegramService {

    void sendErrorToTelegram(Throwable e, WebRequest request, ResponseEntity<?> response);

    void sendMessage(TelegramRequest request);

}
