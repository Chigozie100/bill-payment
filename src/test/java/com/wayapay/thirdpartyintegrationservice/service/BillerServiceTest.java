package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.dto.BillerManagementRequest;
import com.wayapay.thirdpartyintegrationservice.dto.BillerManagementResponse;
import com.wayapay.thirdpartyintegrationservice.dto.BillerResponse;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.Biller;
import com.wayapay.thirdpartyintegrationservice.model.Category;
import com.wayapay.thirdpartyintegrationservice.model.ThirdParty;
import com.wayapay.thirdpartyintegrationservice.repo.BillerRepo;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@RunWith(MockitoJUnitRunner.class)
class BillerServiceTest {

    @Mock
    private BillerRepo billerRepo;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private BillerService billerService;

    private BillerResponse billerResponse;
    private BillerManagementRequest billerManagementRequest;
    private BillerManagementResponse billerManagementResponse;
    private ThirdParty thirdParty;
    private Category category;
    private Biller biller;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        billerManagementRequest = new BillerManagementRequest();
        billerManagementRequest.setBillerAggregatorCode("testBillerAggregatorCode");
        billerManagementRequest.setBillerWayaPayCode("testBillerWaya");
        billerManagementRequest.setCategoryId(Long.parseLong("1"));
        billerManagementRequest.setName("testName");
        billerManagementRequest.setId(Long.parseLong("1"));

        billerManagementResponse = new BillerManagementResponse();
        billerManagementResponse.setAggregatorName(ThirdPartyNames.BAXI);
        billerManagementResponse.setBillerAggregatorCode(billerManagementRequest.getBillerAggregatorCode());
        billerManagementResponse.setBillerWayaPayCode(billerManagementRequest.getBillerWayaPayCode());
        billerManagementResponse.setCategoryId(billerManagementRequest.getCategoryId());
        billerManagementResponse.setCategoryName("testCategory");
        billerManagementResponse.setName(billerManagementRequest.getName());
        billerManagementResponse.setId(billerManagementRequest.getId());

        billerResponse = new BillerResponse();
        billerResponse.setBillerId("testBillerId");
        billerResponse.setBillerName("testBillerName");
        billerResponse.setBillerWayaPayName(billerManagementRequest.getBillerWayaPayCode());
        billerResponse.setCategoryId("testCategoryId");

        thirdParty = new ThirdParty();
        thirdParty.setThirdPartyNames(ThirdPartyNames.BAXI);
        thirdParty.setId(Long.parseLong("1"));

        category = new Category();
        category.setThirdParty(thirdParty);
        category.setName(billerManagementResponse.getCategoryName());
        category.setCategoryWayaPayCode("testCategoryWayaPayCode");
        category.setCategoryAggregatorCode("testCategoryAggregatorCode");
        category.setId(Long.parseLong("1"));

        biller = new Biller();
        biller.setCategory(category);
        biller.setBillerAggregatorCode("testBillerAggregatorCode");
        biller.setBillerWayaPayCode("testWayaPayCode");
        biller.setName("testName");
        biller.setId(Long.parseLong("1"));

    }

    @Order(1)
    @Test
    void getAllActiveBillers() throws ThirdPartyIntegrationException {

        assertThrows(ThirdPartyIntegrationException.class, () -> billerService.getAllActiveBillers(null));

        when(billerRepo.findAllActiveBiller(anyString())).thenReturn(Collections.singletonList(billerResponse));
        assertFalse(billerService.getAllActiveBillers(billerResponse.getCategoryId()).isEmpty());

        when(billerRepo.findAllActiveBiller(anyString())).thenThrow(NullPointerException.class);
        assertThrows(ThirdPartyIntegrationException.class, () -> billerService.getAllActiveBillers(billerResponse.getCategoryId()));
    }

    @Order(2)
    @Test
    void createBiller() throws ThirdPartyIntegrationException {

        when(categoryService.isCategoryIdValid(anyLong())).thenReturn(false);
        assertThrows(ThirdPartyIntegrationException.class, () -> billerService.createBiller(billerManagementRequest));

        when(categoryService.isCategoryIdValid(anyLong())).thenReturn(true);
        when(billerRepo.findByNameAndCategoryId(anyString(), anyLong())).thenReturn(Long.parseLong("1"));
        assertThrows(ThirdPartyIntegrationException.class, () -> billerService.createBiller(billerManagementRequest));

        when(billerRepo.findByNameAndCategoryId(anyString(), anyLong())).thenReturn(Long.parseLong("0"));
        when(billerRepo.findByBillerAggregatorCodeAndCategoryId(anyString(), anyLong())).thenReturn(Long.parseLong("1"));
        assertThrows(ThirdPartyIntegrationException.class, () -> billerService.createBiller(billerManagementRequest));

        when(billerRepo.findByBillerAggregatorCodeAndCategoryId(anyString(), anyLong())).thenReturn(Long.parseLong("0"));
        when(categoryService.findById(anyLong())).thenReturn(Optional.of(category));
        when(billerRepo.save(any(Biller.class))).thenReturn(biller);
        when(categoryService.findThirdPartyByCategoryAggregatorCode(anyString())).thenReturn(Optional.of(ThirdPartyNames.BAXI));
        assertDoesNotThrow(() -> billerService.createBiller(billerManagementRequest));

    }

    @Order(3)
    @Test
    void updateBiller() throws ThirdPartyIntegrationException {

        Long id = billerManagementRequest.getId();
        billerManagementRequest.setId(null);
        assertThrows(ThirdPartyIntegrationException.class, () -> billerService.updateBiller(billerManagementRequest));

        billerManagementRequest.setId(id);
        when(categoryService.isCategoryIdValid(anyLong())).thenReturn(false);
        assertThrows(ThirdPartyIntegrationException.class, () -> billerService.updateBiller(billerManagementRequest));

        when(categoryService.isCategoryIdValid(anyLong())).thenReturn(true);
        when(billerRepo.findById(anyLong())).thenReturn(Optional.of(biller));
        when(billerRepo.findByNameAndCategoryIdNotId(anyString(), anyLong(), anyLong())).thenReturn(Long.parseLong("1"));
        assertThrows(ThirdPartyIntegrationException.class, () -> billerService.updateBiller(billerManagementRequest));

        when(billerRepo.findByNameAndCategoryIdNotId(anyString(), anyLong(), anyLong())).thenReturn(Long.parseLong("0"));
        when(billerRepo.findByBillerAggregatorCodeAndCategoryIdNotId(anyString(), anyLong(), anyLong())).thenReturn(Long.parseLong("1"));
        assertThrows(ThirdPartyIntegrationException.class, () -> billerService.updateBiller(billerManagementRequest));

        when(billerRepo.findByBillerAggregatorCodeAndCategoryIdNotId(anyString(), anyLong(), anyLong())).thenReturn(Long.parseLong("0"));
        when(categoryService.findById(anyLong())).thenReturn(Optional.of(category));
        when(billerRepo.save(any(Biller.class))).thenReturn(biller);
        when(categoryService.findThirdPartyByCategoryAggregatorCode(anyString())).thenReturn(Optional.of(ThirdPartyNames.BAXI));
        assertDoesNotThrow(() -> billerService.updateBiller(billerManagementRequest));

    }

    @Order(4)
    @Test
    void get() {

        assertThrows(ThirdPartyIntegrationException.class, () -> billerService.get(null));

        when(billerRepo.findBillerById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ThirdPartyIntegrationException.class, () -> billerService.get(billerManagementResponse.getId()));

        when(billerRepo.findBillerById(anyLong())).thenReturn(Optional.of(billerManagementResponse));
        assertDoesNotThrow(() -> billerService.get(billerManagementResponse.getId()));
    }

    @Order(5)
    @Test
    void getAll() {

        when(billerRepo.findAllBiller()).thenReturn(Collections.singletonList(billerManagementResponse));
        assertDoesNotThrow(() -> billerService.getAll());

        when(billerRepo.findAllBiller()).thenThrow(NullPointerException.class);
        assertThrows(ThirdPartyIntegrationException.class, () -> billerService.getAll());

    }

    @Order(6)
    @Test
    void toggle() {

        assertThrows(ThirdPartyIntegrationException.class, () -> billerService.toggle(null));

        when(billerRepo.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ThirdPartyIntegrationException.class, () -> billerService.toggle(billerManagementRequest.getId()));

        when(billerRepo.findById(anyLong())).thenReturn(Optional.of(biller));
        when(billerRepo.save(any(Biller.class))).thenReturn(biller);
        when(categoryService.findThirdPartyByCategoryAggregatorCode(anyString())).thenReturn(Optional.of(ThirdPartyNames.BAXI));
        assertDoesNotThrow(() -> billerService.toggle(billerManagementRequest.getId()));

    }

    @Order(7)
    @Test
    void findAllByCategoryId() throws ThirdPartyIntegrationException {

        when(billerRepo.findAllByCategoryId(anyLong())).thenReturn(Collections.singletonList(biller));
        assertFalse(billerService.findAllByCategoryId(Long.parseLong("1")).isEmpty());

        when(billerRepo.findAllByCategoryId(anyLong())).thenThrow(NullPointerException.class);
        assertThrows(ThirdPartyIntegrationException.class, () -> billerService.findAllByCategoryId(Long.parseLong("1")));

    }

    @Order(8)
    @Test
    void deleteAll() {
        assertDoesNotThrow(() -> billerService.deleteAll(Collections.singletonList(biller)));
    }

    @Order(9)
    @Test
    void saveAll() {
        assertDoesNotThrow(() -> billerService.saveAll(Collections.singletonList(biller)));
    }

    @Order(10)
    @Test
    void findById() {

        when(billerRepo.findById(anyLong())).thenReturn(Optional.of(biller));
        assertTrue(billerService.findById(Long.parseLong("1")).isPresent());

        when(billerRepo.findById(anyLong())).thenThrow(NullPointerException.class);
        assertFalse(billerService.findById(Long.parseLong("1")).isPresent());
    }
}