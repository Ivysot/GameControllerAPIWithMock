package project.ApiDio.gameapi.gameLibrary.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import project.ApiDio.gameapi.controllers.GameController;
import project.ApiDio.gameapi.dto.request.GameDTO;
import project.ApiDio.gameapi.dto.request.QuantityDTO;
import project.ApiDio.gameapi.exception.GameNotFoundException;
import project.ApiDio.gameapi.exception.GameStockExceededException;
import project.ApiDio.gameapi.gameLibrary.builder.GameDTOBuilder;
import project.ApiDio.gameapi.services.GameService;

import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static project.ApiDio.gameapi.gameLibrary.utils.JsonConversionUtils.asJsonString;

@ExtendWith(MockitoExtension.class)
public class GameControllerTest {
    private static final String GAME_API_URL_PATH = "/api/v1/games";
    private static final long VALID_GAME_ID = 1L;
    private static final long INVALID_GAME_ID = 2l;
    private static final String GAME_API_SUBPATH_INCREMENT_URL = "/increment";
    private static final String GAME_API_SUBPATH_DECREMENT_URL = "/decrement";

    private MockMvc mockMvc;

    @Mock
    private GameService gameService;

    @InjectMocks
    private GameController gameController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(gameController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setViewResolvers((s, locale) -> new MappingJackson2JsonView())
                .build();
    }

    @Test
    void whenPOSTIsCalledThenAGameIsCreated() throws Exception {
        // given
        GameDTO gameDTO = GameDTOBuilder.builder().build().toGameDTO();

        // when
        when(gameService.createGame(gameDTO)).thenReturn(gameDTO);

        // then
        mockMvc.perform(post(GAME_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(gameDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameName", is(gameDTO.getGameName())))
                .andExpect(jsonPath("$.producer", is(gameDTO.getGameProducer())))
                .andExpect(jsonPath("$.console", is(gameDTO.getGameConsole())));
    }

    @Test
    void whenPOSTIsCalledWithoutRequiredFieldThenAnErrorIsReturned() throws Exception {
        // given
        GameDTO gameDTO = GameDTOBuilder.builder().build().toGameDTO();
        gameDTO.setGameProducer(null);

        // then
        mockMvc.perform(post(GAME_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(gameDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGETIsCalledWithValidNameThenOkStatusIsReturned() throws Exception {
        // given
        GameDTO gameDTO = GameDTOBuilder.builder().build().toGameDTO();

        //when
        when(gameDTO.findByName(gameDTO.getGameName())).thenReturn(gameDTO);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get(GAME_API_URL_PATH + "/" + gameDTO.getGameName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameName", is(gameDTO.getGameName())))
                .andExpect(jsonPath("$.producer", is(gameDTO.getGameProducer())))
                .andExpect(jsonPath("$.console", is(gameDTO.getGameConsole())));
    }

    @Test
    void whenGETIsCalledWithoutRegisteredNameThenNotFoundStatusIsReturned() throws Exception {
        // given
        GameDTO gameDTO = GameDTOBuilder.builder().build().toGameDTO();

        //when
        when(gameService.findByName(gameDTO.getGameName())).thenThrow(GameNotFoundException.class);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get(GAME_API_URL_PATH + "/" + gameDTO.getGameName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGETListWithGamesIsCalledThenOkStatusIsReturned() throws Exception {
        // given
        GameDTO gameDTO = GameDTOBuilder.builder().build().toGameDTO();

        //when
        when(gameService.listGames()).thenReturn(Collections.singletonList(gameDTO));

        // then
        mockMvc.perform(MockMvcRequestBuilders.get(GAME_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameName", is(gameDTO.getGameName())))
                .andExpect(jsonPath("$.gameProducer", is(gameDTO.getGameProducer())))
                .andExpect(jsonPath("$.gameConsole", is(gameDTO.getGameConsole())));
    }

    @Test
    void whenGETListWithoutGamesIsCalledThenOkStatusIsReturned() throws Exception {
        // given
        GameDTO gameDTO = GameDTOBuilder.builder().build().toGameDTO();

        //when
        when(gameService.listGames()).thenReturn(Collections.singletonList(gameDTO));

        // then
        mockMvc.perform(MockMvcRequestBuilders.get(GAME_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void whenDELETEIsCalledWithValidIdThenNoContentStatusIsReturned() throws Exception {
        // given
        GameDTO gameDTO = GameDTOBuilder.builder().build().toGameDTO();

        //when
        doNothing().when(gameService).deleteById(gameDTO.getId());

        // then
        mockMvc.perform(MockMvcRequestBuilders.delete(GAME_API_URL_PATH + "/" + gameDTO.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void whenDELETEIsCalledWithInvalidIdThenNotFoundStatusIsReturned() throws Exception {
        //when
        doThrow(GameNotFoundException.class).when(gameService).deleteById(INVALID_GAME_ID);

        // then
        mockMvc.perform(MockMvcRequestBuilders.delete(GAME_API_URL_PATH + "/" + INVALID_GAME_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenPATCHIsCalledToIncrementDiscountThenOKstatusIsReturned() throws Exception {
        QuantityDTO quantityDTO = QuantityDTO.builder()
                .quantity(10)
                .build();

        GameDTO gameDTO = GameDTOBuilder.builder().build().toGameDTO();
        gameDTO.setQuantity(gameDTO.getQuantity() + quantityDTO.getQuantity());

        when(gameService.increment(VALID_GAME_ID, quantityDTO.getQuantity())).thenReturn(gameDTO);

        mockMvc.perform(patch(GAME_API_URL_PATH + "/" + VALID_GAME_ID + GAME_API_SUBPATH_INCREMENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(quantityDTO))).andExpect(status().isOk())
                .andExpect(jsonPath("$.gameName", is(gameDTO.getGameName())))
                .andExpect(jsonPath("$.producer", is(gameDTO.getGameProducer())))
                .andExpect(jsonPath("$.console", is(gameDTO.getGameConsole())))
                .andExpect(jsonPath("$.quantity", is(gameDTO.getQuantity())));
    }

//      @Test
//    void whenPATCHIsCalledToIncrementGreatherThanMaxThenBadRequestStatusIsReturned() throws Exception {
//        QuantityDTO quantityDTO = QuantityDTO.builder()
//               .quantity(30)
//                .build();
//
//        GameDTO gameDTO = GameDTOBuilder.builder().build().toGameDTO();
//        gameDTO.setQuantity(gameDTO.getQuantity() + quantityDTO.getQuantity());
//
//        when(GameService.increment(VALID_GAME_ID, quantityDTO.getQuantity())).thenThrow(GameStockExceededException.class);
//
//        mockMvc.perform(patch(GAME_API_URL_PATH + "/" + VALID_GAME_ID + GAME_API_SUBPATH_INCREMENT_URL)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(asJsonString(quantityDTO))).andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void whenPATCHIsCalledWithInvalidGameIdToIncrementThenNotFoundStatusIsReturned() throws Exception {
//        QuantityDTO quantityDTO = QuantityDTO.builder()
//                .quantity(30)
//                .build();
//
//        when(gameService.increment(INVALID_GAME_ID, quantityDTO.getQuantity())).thenThrow(GameNotFoundException.class);
//        mockMvc.perform(patch(GAME_API_URL_PATH + "/" + INVALID_GAME_ID + GAME_API_SUBPATH_INCREMENT_URL)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(asJsonString(quantityDTO)))
//                .andExpect(status().isNotFound());
//    }
//
//    @SneakyThrows
//    @Test
//    void whenPATCHIsCalledToDecrementDiscountThenOKstatusIsReturned() throws Exception {
//        QuantityDTO quantityDTO = QuantityDTO.builder()
//                .quantity(5)
//                .build();
//
//        GameDTO gameDTO = GameDTOBuilder.builder().build().toGameDTO();
//        gameDTO.setQuantity(gameDTO.getQuantity() + quantityDTO.getQuantity());
//
//        when(gameService.decrement(VALID_GAME_ID, quantityDTO.getQuantity())).thenReturn(gameDTO);
//
//        mockMvc.perform(patch(GAME_API_URL_PATH + "/" + VALID_GAME_ID + GAME_API_SUBPATH_DECREMENT_URL)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(asJsonString(quantityDTO))).andExpect(status().isOk())
//                .andExpect(jsonPath("$.gameName", is(gameDTO.getGameName())))
//                .andExpect(jsonPath("$.producer", is(gameDTO.getGameProducer())))
//                .andExpect(jsonPath("$.console", is(gameDTO.getGameConsole())))
//                .andExpect(jsonPath("$.quantity", is(gameDTO.getQuantity())));
//    }
//
//    @SneakyThrows
//    @Test
//    void whenPATCHIsCalledToDEcrementLowerThanZeroThenBadRequestStatusIsReturned() throws Exception {
//        QuantityDTO quantityDTO = QuantityDTO.builder()
//                .quantity(60)
//                .build();
//
//        GameDTO gameDTO = GameDTOBuilder.builder().build().toGameDTO();
//        gameDTO.setQuantity(gameDTO.getQuantity() + quantityDTO.getQuantity());
//
//        when(gameService.decrement(VALID_GAME_ID, quantityDTO.getQuantity())).thenThrow(GameStockExceededException.class);
//
//        mockMvc.perform(patch(GAME_API_URL_PATH + "/" + VALID_GAME_ID + GAME_API_SUBPATH_DECREMENT_URL)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(asJsonString(quantityDTO))).andExpect(status().isBadRequest());
//    }
//
//    @SneakyThrows
//    @Test
//    void whenPATCHIsCalledWithInvalidGameIdToDecrementThenNotFoundStatusIsReturned() throws Exception {
//        QuantityDTO quantityDTO = QuantityDTO.builder()
//                .quantity(5)
//                .build();
//
//        when(gameService.decrement(INVALID_GAME_ID, quantityDTO.getQuantity())).thenThrow(GameNotFoundException.class);
//        mockMvc.perform(patch(GAME_API_URL_PATH + "/" + INVALID_GAME_ID + GAME_API_SUBPATH_DECREMENT_URL)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(asJsonString(quantityDTO)))
//                .andExpect(status().isNotFound());
//    }
//}
//
//}
