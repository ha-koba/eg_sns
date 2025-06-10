package com.example.eg_sns.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.example.eg_sns.entity.TopicImages;

public interface TopicImagesRepository extends PagingAndSortingRepository<TopicImages, Long>, CrudRepository<TopicImages, Long> {

}
