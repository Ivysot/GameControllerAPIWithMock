package project.ApiDio.gameapi.services;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.ApiDio.gameapi.dto.mapper.GameMapper;
import project.ApiDio.gameapi.dto.request.GameDTO;
import project.ApiDio.gameapi.entities.Game;
import project.ApiDio.gameapi.exception.GameAlreadyRegisteredException;
import project.ApiDio.gameapi.exception.GameNotFoundException;
import project.ApiDio.gameapi.exception.GameStockExceededException;
import project.ApiDio.gameapi.repositories.GameRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class GameService {

    private final GameRepository gameRepository;

    private final GameMapper gameMapper = GameMapper.INSTANCE;

    public GameDTO createGame(GameDTO gameDTO) throws GameAlreadyRegisteredException {
        verifyIfIsAlreadyRegistered(gameDTO.getGameName());
        Game game = gameMapper.toModel(gameDTO);
        Game savedGame = gameRepository.save(game);

        return gameMapper.toDTO(savedGame);

    }

    public GameDTO findByName(String gameName) throws GameNotFoundException {
        Game foundGame = gameRepository.findByName(gameName)
                .orElseThrow(() -> new GameNotFoundException(gameName));
        return gameMapper.toDTO(foundGame);
    }

    public List<GameDTO> listGames() {
        List<Game> games = gameRepository.findAll();
        return games.stream()
                .map(gameMapper::toDTO)
                .collect(Collectors.toList());
    }


    public void deleteById(Long id) throws GameNotFoundException {
        gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException(id));

        gameRepository.deleteById(id);
    }

    private void verifyIfIsAlreadyRegistered(String name) throws GameAlreadyRegisteredException {
        Optional<Game> optSavedGame = gameRepository.findByName(name);
        if (optSavedGame.isPresent()) {
            throw new GameAlreadyRegisteredException(name);
        }
    }

    private Game verifyIfExists(Long id) throws GameNotFoundException {
        return gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException(id));
    }


    public GameDTO increment(Long id, int quantityToIncrement) throws GameNotFoundException, GameStockExceededException {
        Game beerToIncrementStock = verifyIfExists(id);
        int quantityAfterIncrement = quantityToIncrement + beerToIncrementStock.getQuantity();
        if (quantityAfterIncrement <= beerToIncrementStock.getMax()) {
            beerToIncrementStock.setQuantity(beerToIncrementStock.getQuantity() + quantityToIncrement);
            Game incrementedGameStock = gameRepository.save(beerToIncrementStock);
            return gameMapper.toDTO(incrementedGameStock);
        }
        throw new GameStockExceededException(id, quantityToIncrement);
    }
}