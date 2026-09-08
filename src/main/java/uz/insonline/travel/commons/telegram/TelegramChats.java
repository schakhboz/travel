package uz.insonline.travel.commons.telegram;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TelegramChats {

    SELF("self"),
    ERRORS("errors");

    private final String chatName;

}
