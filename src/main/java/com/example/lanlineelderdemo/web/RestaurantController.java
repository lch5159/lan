package com.example.lanlineelderdemo.web;

import com.example.lanlineelderdemo.web.form.review.ReviewCreateForm;
import com.example.lanlineelderdemo.utils.enums.EnumMapper;
import com.example.lanlineelderdemo.utils.enums.EnumValue;
import com.example.lanlineelderdemo.utils.ExcelFileManager;
import com.example.lanlineelderdemo.domain.SearchCondition;
import com.example.lanlineelderdemo.restaurant.dto.controller.ShowRestaurantDetailsResponseDto;
import com.example.lanlineelderdemo.domain.menu.OpenType;
import com.example.lanlineelderdemo.menu.MenuService;
import com.example.lanlineelderdemo.restaurant.RestaurantService;
import com.example.lanlineelderdemo.web.form.menu.MenuForm;
import com.example.lanlineelderdemo.restaurant.dto.service.RestaurantCreateServiceRequestDto;
import com.example.lanlineelderdemo.restaurant.dto.service.RestaurantRecommendMenuDto;
import com.example.lanlineelderdemo.restaurant.dto.service.SearchRestaurantResponseDto;
import com.example.lanlineelderdemo.restaurant.dto.service.RestaurantResponseDto;
import com.example.lanlineelderdemo.review.ReviewService;
import com.example.lanlineelderdemo.web.form.restaurant.SearchForm;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class RestaurantController {
    private final EnumMapper enumMapper;
    private final RestaurantService restaurantService;
    private final MenuService menuService;
    private final ReviewService reviewService;

    @ModelAttribute("locations")
    public Map<String, String> locations() {
        Map<String, List<EnumValue>> enums = enumMapper.get("locations");
        List<EnumValue> enumValues = enums.get("locations");

        Map<String, String> locations = new LinkedHashMap<>();
        for (EnumValue enumValue : enumValues) {
            locations.put(enumValue.getKey(), enumValue.getValue());
        }
        return locations;
    }

    @ModelAttribute("foodCategories")
    public Map<String, String> foodCategories() {
        Map<String, List<EnumValue>> enums = enumMapper.get("foodCategories");
        List<EnumValue> enumValues = enums.get("foodCategories");
        Map<String, String> foodCategories = new LinkedHashMap<>();
        for (EnumValue enumValue : enumValues) {
            foodCategories.put(enumValue.getKey(), enumValue.getValue());
        }
        return foodCategories;
    }

    @ModelAttribute("openTypes")
    public Map<String, String> openTypes() {
        Map<String, List<EnumValue>> enums = enumMapper.get("openTypes");
        List<EnumValue> enumValues = enums.get("openTypes");

        Map<String, String> foodCategories = new LinkedHashMap<>();
        for (EnumValue enumValue : enumValues) {
            if (enumValue.getKey() == OpenType.BOTH.getKey()) {
                continue;
            }
            foodCategories.put(enumValue.getKey(), enumValue.getValue());
        }
        return foodCategories;
    }

    /**
     * ?????? ?????????
     */
    @GetMapping("/search")
    public String searchRestaurantsForm(@ModelAttribute("searchForm") SearchForm form) {
        return "restaurants/searchForm";
    }

    /**
     * ??????
     */
    @PostMapping("search")
    public String searchRestaurants(
            @ModelAttribute("searchForm") SearchForm searchForm, Model model) {
        SearchCondition searchCondition = searchForm.toEntity();
        List<SearchRestaurantResponseDto> results = restaurantService.searchRestaurants(searchCondition);
        model.addAttribute("results",results);
        return "restaurants/resultPage";
    }

    /**
     * ????????????
     */
    @GetMapping("/restaurants/{restaurantId}")
    public String showRestaurantDetails(@PathVariable Long restaurantId, Model model,
                                        @ModelAttribute ReviewCreateForm reviewCreateForm) {
        model.addAttribute("restaurant", makeRestaurantDetailInfo(restaurantId));
        model.addAttribute("reviews", reviewService.inqueryRestaurantReviews(restaurantId));
        return "restaurants/detailPage";
        // ????????? ???????????? ???????????? ???????????? ?????????.
    }

    private ShowRestaurantDetailsResponseDto makeRestaurantDetailInfo(Long restaurantId) {
        RestaurantResponseDto inqueryRestaurantResponse = restaurantService.inqueryRestaurant(restaurantId);
        RestaurantRecommendMenuDto recommendMenu = menuService.findRestaurantRecommendMenu(restaurantId);
        ShowRestaurantDetailsResponseDto restaurant = ShowRestaurantDetailsResponseDto.create(inqueryRestaurantResponse, recommendMenu);
        return restaurant;
    }

    /**
     * ?????? ????????? GetMapping (Admin???) ?????? ???????????? ?????? ?????????????????? ????????????. ?????? ?????? ????????????.
     * TODO ?????? ????????? ???????????? ????????? ???????????? ?????????. ?????? keep
     */
    @GetMapping("/restaurants/new")
    public String registerRestaurantForm(@ModelAttribute MultipartFile file) {
        return "restaurants/registerForm";
    }

    /**
     * ?????? (Admin???)
     * 201 created Ok / ????????? ?????? ?????? ?????? ????????????
     * ??????????????? ???????????? ?????? http ???????????? ??? ????????? ?????????.
     */
    @PostMapping("/restaurants")
    public String registerRestaurantByAdmin(@ModelAttribute MultipartFile file) throws IOException{
        Sheet worksheet = ExcelFileManager.validateExcelFileIsAvailable(file);

        List<RestaurantCreateServiceRequestDto> dataList = new ArrayList<>();
        for (int i = 1; i < worksheet.getPhysicalNumberOfRows(); i++) {
            Row row = worksheet.getRow(i);
            dataList.add(new RestaurantCreateServiceRequestDto(row));
        }
        restaurantService.registerRestaurants(dataList);
        return "redirect:/";
    }

    /**
     * ?????? ?????? ????????? GetMapping (Admin???) ?????? ???????????? ?????? ?????????????????? ????????????. ?????? ?????? ????????????.
     */
    @GetMapping("/menu/new")
    public String registerMenuForm(@ModelAttribute MultipartFile file) {
        return "registerMenuForm";
    }

    /**
     * ?????? ?????? (Admin???)
     */
    @PostMapping("/menu")
    public String registerMenus(@ModelAttribute MultipartFile file) throws IOException{
        List<MenuForm> dataList = new ArrayList<>();
        Sheet worksheet = ExcelFileManager.validateExcelFileIsAvailable(file);
        for (int i = 1; i < worksheet.getPhysicalNumberOfRows(); i++) { // 4
            Row row = worksheet.getRow(i);
            dataList.add(new MenuForm(row));
            //TODO ?????? ?????? ????????? ????????? ????????? ???????????????.
            // ?????? ?????? ??????????????? ?????? ???????????? menuService.registerMenu??? ??????
        }
        menuService.registerMenus(dataList);
        return "redirect:/";
    }

    /**
     * ??????
     * ?????? http ??????????????? / ????????? ????????? ???????
     */
}
