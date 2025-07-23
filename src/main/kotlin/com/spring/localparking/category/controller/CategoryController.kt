package com.spring.localparking.category.controller

import com.spring.localparking.category.service.CategoryService
import com.spring.localparking.global.response.ResponseDto
import com.spring.localparking.global.response.SuccessCode
import com.spring.localparking.category.dto.CategoryResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "카테고리 컨트롤러", description = "카테고리 관련 API입니다.")
@RestController
@RequestMapping("/category")
class CategoryController (
    private val categoryService: CategoryService
){

    @Operation(summary = "상위 카테고리 조회", description = "상위 카테고리를 조회하는 API입니다.")
    @GetMapping("/parent")
    fun getCategories():
            ResponseEntity<ResponseDto<CategoryResponse>> {
        val response = categoryService.getParentCategories()
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, response))
    }

    @Operation(summary = "전체 카테고리 조회", description = "전체 카테고리를 조회하는 API입니다.")
    @GetMapping("/all")
    fun getChildCategories():
            ResponseEntity<ResponseDto<CategoryResponse>> {
        val response = categoryService.getAllCategories()
        return ResponseEntity.ok(ResponseDto.from(SuccessCode.OK, response))
    }
}