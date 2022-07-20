package com.appsdeveloperblog.app.ws.io.repositories;

import com.appsdeveloperblog.app.ws.io.entity.UserEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends PagingAndSortingRepository<UserEntity,Long> {
    //CrudRepository<T,ID>
    //T is the class of the object that needs to be processed (ex. UserEntity)
    //ID is the data type of the "id" field, which is a long


    //PagingAndSortingRepository<T,ID>
    //We use this one for pagination and it contains the same methods as Crud while including those for pagination ops
    //Any method you described here will also remain the same


    //You don't need to provide any method in this repo to be able to read, update, or delete
    // but if you want to, you can add a custom implementation such as findByX or deleteByX etc.

    UserEntity findByEmail(String email);

    UserEntity findByUserId(String userId);
}
