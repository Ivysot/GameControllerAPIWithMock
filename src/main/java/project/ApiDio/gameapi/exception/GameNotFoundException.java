package project.ApiDio.gameapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GameNotFoundException extends Exception {

    public GameNotFoundException(Long id) {
        super(String.format("game with ID %s not found!", id));
    }
    public GameNotFoundException(String gameName) {
        super(String.format("Beer with name %s not found in the system.", gameName));
    }
}
