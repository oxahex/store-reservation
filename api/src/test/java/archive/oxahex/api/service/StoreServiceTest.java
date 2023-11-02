package archive.oxahex.api.service;

import archive.oxahex.api.dto.SortType;
import archive.oxahex.api.dto.request.StoreModifyRequest;
import archive.oxahex.api.dto.request.StoreRegisterRequest;
import archive.oxahex.api.exception.CustomException;
import archive.oxahex.api.exception.ErrorType;
import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.Reservation;
import archive.oxahex.domain.entity.Store;
import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.repository.PartnersRepository;
import archive.oxahex.domain.repository.ReservationRepository;
import archive.oxahex.domain.repository.StoreRepository;
import archive.oxahex.domain.type.ReservationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Transactional
class StoreServiceTest {

    @InjectMocks
    StoreService storeService;

    @Mock
    StoreRepository storeRepository;

    @Mock
    PartnersRepository partnersRepository;

    @Mock
    ReservationRepository reservationRepository;

    @Test
    @DisplayName("기존에 파트너스 등록을 하지 않은 경우 매장을 등록할 수 없다.")
    void registerStore_failure_partners_not_found() {

        // given
        User user = User.builder().build();
        StoreRegisterRequest request = new StoreRegisterRequest();

        given(partnersRepository.findByUser(any(User.class)))
                .willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.registerStore(user, request));

        // then
        assertEquals(ErrorType.PARTNERS_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());
        assertEquals(ErrorType.PARTNERS_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
    }

    @Test
    @DisplayName("이미 등록한 매장(사업자 번호가 동일한 경우)은 파트너스에 다시 등록할 수 없다.")
    void registerStore_failure_already_exist_store() {

        // given
        User user = User.builder().build();
        StoreRegisterRequest request = new StoreRegisterRequest();
        request.setBusinessNumber("12345678901");

        given(partnersRepository.findByUser(any(User.class)))
                .willReturn(Optional.of(Partners.builder().build()));
        given(storeRepository.existsByBusinessNumber(anyString()))
                .willReturn(true);

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.registerStore(user, request));

        // then
        assertEquals(ErrorType.ALREADY_EXIST_STORE.getErrorMessage(), exception.getErrorMessage());
        assertEquals(ErrorType.ALREADY_EXIST_STORE.getHttpStatus(), exception.getHttpStatus());
    }

    @Test
    @DisplayName("매장 등록 시 해당 파트너스에 매장이 저장된다.")
    void registerStore_() {

        // given
        User user = User.builder().build();
        StoreRegisterRequest request = new StoreRegisterRequest();
        request.setName("매장이름1");
        request.setBusinessNumber("12345678901");

        Partners partners = Partners.builder()
                        .name("파트너스").build();

        given(partnersRepository.findByUser(any(User.class)))
                .willReturn(Optional.of(partners));

        given(storeRepository.existsByBusinessNumber(anyString()))
                .willReturn(Boolean.FALSE);

        Store store = Store.builder()
                .name(request.getName())
                .partners(partners)
                .businessNumber(request.getBusinessNumber())
                .build();

        given(storeRepository.save(any(Store.class)))
                .willReturn(store);

        // when
        Store savedStore = storeService.registerStore(user, request);

        // then
        assertTrue(savedStore.getPartners().getStores().contains(store));
        assertEquals(savedStore.getPartners().getName(), "파트너스");

    }

    @Test
    @DisplayName("유저의 파트너스 정보가 없는 경우 매장 수정이 불가능하다.")
    void modifyStore_failure_partners_not_found() {
        // given
        User user = User.builder().build();
        StoreModifyRequest request = new StoreModifyRequest();
        request.setName("변경할 매장 이름");
        request.setAddress("변경할 주소");
        request.setDescription("변경할 설명");
        request.setTableCount(10);

        given(partnersRepository.findByUser(any(User.class)))
                .willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.modifyStore(user, 1L, 1L, request));

        // then
        assertEquals(ErrorType.PARTNERS_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
        assertEquals(ErrorType.PARTNERS_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());
    }

    @Test
    @DisplayName("해당 매장의 소유주가 아닌 경우 매장 수정이 불가능하다.")
    void modifyStore_failure_store_access_denied() {
        // given
        User user = User.builder().build();
        StoreModifyRequest request = new StoreModifyRequest();
        request.setName("변경할 매장 이름");
        request.setAddress("변경할 주소");
        request.setDescription("변경할 설명");
        request.setTableCount(10);
        Partners partners1 = Partners.builder().name("partners1").build();
        Partners partners2 = Partners.builder().name("partners2").build();

        Store store = Store.builder()
                .name("store")
                .partners(partners1)
                .build();

        // 실제 소유한 파트너스와 매장과 연결된 파트너스가 다름
        given(partnersRepository.findByUser(any(User.class)))
                .willReturn(Optional.of(partners2));

        given(storeRepository.findById(anyLong()))
                .willReturn(Optional.of(store));


        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.modifyStore(user, 1L, 1L, request));

        // then
        assertEquals(ErrorType.STORE_ACCESS_DENIED.getHttpStatus(), exception.getHttpStatus());
        assertEquals(ErrorType.STORE_ACCESS_DENIED.getErrorMessage(), exception.getErrorMessage());
    }

    @Test
    @DisplayName("해당 매장 정보가 없는 경우 매장 정보 수정이 불가능하다.")
    void modifyStore_failure_store_not_found() {

        // given
        User user = User.builder().build();
        StoreModifyRequest request = new StoreModifyRequest();
        request.setName("변경할 매장 이름");
        request.setAddress("변경할 주소");
        request.setDescription("변경할 설명");
        request.setTableCount(10);
        Partners partners1 = Partners.builder().name("partners1").build();
        Partners partners2 = Partners.builder().name("partners2").build();

        Store store = Store.builder()
                .name("store")
                .partners(partners1)
                .build();

        // store 없음
        given(partnersRepository.findByUser(any(User.class)))
                .willReturn(Optional.of(partners1));

        given(storeRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.modifyStore(user, 1L, 1L, request));

        // then
        assertEquals(ErrorType.STORE_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
        assertEquals(ErrorType.STORE_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());

    }

    @Test
    @DisplayName("매장 정보 수정을 완료하는 경우, 변경된 매장 정보를 반환한다.")
    void modifyStore_success() {

        // given
        User user = User.builder().build();
        StoreModifyRequest request = new StoreModifyRequest();
        request.setName("변경할 매장 이름");
        request.setAddress("변경할 주소");
        request.setDescription("변경할 설명");
        request.setTableCount(10);
        Partners partners1 = Partners.builder().name("partners1").build();
        Partners partners2 = Partners.builder().name("partners2").build();

        Store store = Store.builder()
                .name("store")
                .address("address")
                .description("description")
                .tableCount(1)
                .partners(partners1)
                .build();

        // store 없음
        given(partnersRepository.findByUser(any(User.class)))
                .willReturn(Optional.of(partners1));

        given(storeRepository.findById(anyLong()))
                .willReturn(Optional.of(store));

        given(storeRepository.save(any(Store.class)))
                .willReturn(store);


        // when
        Store modifiedStore = storeService.modifyStore(user, 1L, 1L, request);

        // then
        assertEquals(modifiedStore.getName(),"변경할 매장 이름");
        assertEquals(modifiedStore.getAddress(),"변경할 주소");
        assertEquals(modifiedStore.getDescription(), "변경할 설명");
        assertEquals(modifiedStore.getPartners(), store.getPartners());
        assertEquals(modifiedStore.getBusinessNumber(), store.getBusinessNumber());

    }

    @Test
    @DisplayName("해당 유저의 파트너스 정보가 없는 경우 매장 삭제가 불가능하다.")
    void deleteStore_failure_partners_not_found() {

        // given
        User user = User.builder().build();
        Partners partners1 = Partners.builder().name("partners1").build();
        Partners partners2 = Partners.builder().name("partners2").build();
        Store store = Store.builder().partners(partners1).build();

        given(partnersRepository.findByUser(any(User.class)))
                .willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.deleteStore(user, 1L, 1L));

        // then
        assertEquals(ErrorType.PARTNERS_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
        assertEquals(ErrorType.PARTNERS_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());
    }

    @Test
    @DisplayName("해당 매장의 소유주가 아닌 경우 매장 삭제가 불가능하다.")
    void deleteStore_failure_store_access_denied() {

        // given
        User user = User.builder().build();
        Partners partners1 = Partners.builder().name("partners1").build();
        Partners partners2 = Partners.builder().name("partners2").build();
        Store store = Store.builder().partners(partners1).build();

        given(partnersRepository.findByUser(any(User.class)))
                .willReturn(Optional.of(partners2));

        given(storeRepository.findById(anyLong()))
                .willReturn(Optional.of(store));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.deleteStore(user, 1L, 1L));

        // then
        assertEquals(ErrorType.STORE_ACCESS_DENIED.getHttpStatus(), exception.getHttpStatus());
        assertEquals(ErrorType.STORE_ACCESS_DENIED.getErrorMessage(), exception.getErrorMessage());
    }

    @Test
    @DisplayName("해당 매장이 존재하지 않는 경우 매장 삭제가 불가능하다.")
    void deleteStore_failure_store_not_found() {

        // given
        User user = User.builder().build();
        Partners partners1 = Partners.builder().name("partners1").build();
        Partners partners2 = Partners.builder().name("partners2").build();
        Store store = Store.builder().partners(partners1).build();

        given(partnersRepository.findByUser(any(User.class)))
                .willReturn(Optional.of(partners1));

        given(storeRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.deleteStore(user, 1L, 1L));

        // then
        assertEquals(ErrorType.STORE_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
        assertEquals(ErrorType.STORE_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());
    }

    @Test
    @DisplayName("예약 대기 중인 예약이 존재하는 경우 매장 삭제가 불가능하다.")
    void deleteStore_failure_reservation() {

        // given
        User user = User.builder().build();
        Partners partners1 = Partners.builder().name("partners1").build();
        Partners partners2 = Partners.builder().name("partners2").build();
        Store store = Store.builder().partners(partners1).build();

        given(partnersRepository.findByUser(any(User.class)))
                .willReturn(Optional.of(partners1));

        given(storeRepository.findById(anyLong()))
                .willReturn(Optional.of(store));

        Reservation reservation1 = Reservation.builder().build();
        Reservation reservation2 = Reservation.builder().build();

        given(reservationRepository.findAllByStoreAndStatus(any(Store.class), any(ReservationStatus.class)))
                .willReturn(new ArrayList<>(List.of(reservation1, reservation2)));


        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.deleteStore(user, 1L, 1L));

        // then
        assertEquals(ErrorType.FAIL_TO_DELETE_STORE.getErrorMessage(), exception.getErrorMessage());
        assertEquals(ErrorType.FAIL_TO_DELETE_STORE.getHttpStatus(), exception.getHttpStatus());
    }

    @Test
    @DisplayName("매장 삭제를 완료하는 경우 삭제한 매장 정보를 반환한다.")
    void deleteStore_success() {
        // given
        User user = User.builder().build();
        Partners partners1 = Partners.builder().name("partners1").build();
        Partners partners2 = Partners.builder().name("partners2").build();
        Store store = Store.builder().partners(partners1).build();

        given(partnersRepository.findByUser(any(User.class)))
                .willReturn(Optional.of(partners1));

        given(storeRepository.findById(anyLong()))
                .willReturn(Optional.of(store));


        // when
        Store deletedStore =
                storeService.deleteStore(user, 1L, 1L);

        // then
        verify(storeRepository, times(1)).delete(store);

    }

    @Test
    @DisplayName("ASC 타입 조회 시 등록 순서대로 상점리스트를 반환한다.")
    void getAllStore_asc() {

        // given
        SortType sortType = SortType.ASC;

        // when
        List<Store> stores = storeService.getAllStore(sortType);

        // then
        verify(storeRepository, times(1)).findAllByOrderByRegisteredDateAsc();
        verify(storeRepository, times(0)).findAllByOrderByReviewCountAsc();
    }

    @Test
    @DisplayName("REVIEW_COUNT 타입 조회 시 리뷰 수가 많은 순서대로 상점 리스트를 반환한다.")
    void getAllStore_review_count() {

        // given
        SortType sortType = SortType.REVIEW_COUNT;

        // when
        List<Store> stores = storeService.getAllStore(sortType);

        // then
        verify(storeRepository, times(0)).findAllByOrderByRegisteredDateAsc();
        verify(storeRepository, times(1)).findAllByOrderByReviewCountAsc();
    }

    @Test
    @DisplayName("매장 상세정보 조회 시, 해당 매장이 없는 경우 NOT FOUND 예외가 발생한다.")
    void getStore_failure_store_not_found() {

        // given
        given(storeRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> storeService.getStore(1L));

        // then
        assertEquals(ErrorType.STORE_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
        assertEquals(ErrorType.STORE_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());
    }
}