package com.example.demo.dto.helper;


import com.example.demo.exceptions.ApiError;
import org.modelmapper.ModelMapper;
import org.modelmapper.internal.util.Assert;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

public class ResponseDTO<T> extends HttpEntity<T> {

    private static final ModelMapper modelMapper = new ModelMapper();

    private HttpStatus status;

    ResponseDTO(T body, HttpStatus status) {
        super(body);
        this.status = status;
    }

    public static ResponseDTO.Builder accepted() {
        return status(HttpStatus.ACCEPTED);
    }

    public static Builder badRequest() {
        return status(HttpStatus.BAD_REQUEST);
    }

    public static Builder conflict() { return status(HttpStatus.CONFLICT); }

    public static Builder status(HttpStatus status) {
        return new BodyBuilder(status);
    }

    public interface Builder {

        <T> ResponseDTO<T> empty();

        <T> ResponseDTO<T> error(ApiError error);

        <T> ResponseDTO<T> convertTo(Object entity, Class<T> aClass);

        <T> ResponseDTO<List<T>> convertToList(List<?> entityList, Class<T> aClass);
    }

    // yaes
    private static class BodyBuilder implements Builder {
        private HttpStatus status;

        public BodyBuilder(HttpStatus status) {
            this.status = status;
        }

        @Override
        public <T> ResponseDTO<T> convertTo(Object entity, Class<T> aClass) {
            Assert.notNull(AnnotationUtils.getAnnotation(aClass, DTO.class),
                    "Type should contain DTO annotation");

            return new ResponseDTO<T>(modelMapper.map(entity, aClass), this.status);
        }

        @Override
        public <T> ResponseDTO<List<T>> convertToList(List<?> entityList, Class<T> aClass) {
            Assert.notNull(AnnotationUtils.getAnnotation(aClass, DTO.class),
                    "Type should contain DTO annotation");

            return new ResponseDTO<>(entityList.stream()
                    .map(entity -> modelMapper.map(entity, aClass))
                    .collect(Collectors.toList()), this.status);
        }

        @Override
        public <T> ResponseDTO<T> empty() {
            return new ResponseDTO<T>((T) "Hi", this.status);
        }

        @Override
        public <T> ResponseDTO<T> error(ApiError error) {
            return new ResponseDTO<T>((T) error, this.status);
        }
    }
}