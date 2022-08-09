package com.appsdeveloperblog.app.ws.io.repositories;

import com.appsdeveloperblog.app.ws.io.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

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

    UserEntity findUserByEmailVerificationToken(String token);

    @Query(value = "select * from Users u where u.EMAIL_VERIFICATION_STATUS=TRUE",
            countQuery = "select count(*) from Users u where u.EMAIL_VERIFICATION_STATUS=TRUE",
            nativeQuery = true)
    Page<UserEntity> findAllUsersWithVerifiedEmail(Pageable pageRequest);

    //Query with positional parameter
    @Query(value = "select * from Users u where u.first_name=?1", nativeQuery = true)
    List<UserEntity> findUserByFirstName(String firstName);

    //Query with named parameter
    @Query(value = "select * from Users u where u.last_name= :lastName", nativeQuery = true)
    List<UserEntity> findUserByLastName(@Param("lastName") String lastName);

    //Query with LIKE
    @Query(value = "select * from Users u where first_name LIKE %:keyword% or last_name LIKE %:keyword%", nativeQuery = true)
    List<UserEntity> findUserByKeyword(@Param("keyword") String lastName);

    //Query with LIKE and Just Returning Specific Columns
    @Query(value = "select u.first_name,u.last_name from Users u where u.first_name LIKE %:keyword% or u.last_name LIKE %:keyword%", nativeQuery = true)
    List<Object[]> findUserFirstNameAndLastNameByKeyword(@Param("keyword") String lastName);

    //Update Query
    @Transactional
    @Modifying
    @Query(value = "update Users u set u.EMAIL_VERIFICATION_STATUS=:emailVerificationStatus where u.user_id=:userId", nativeQuery = true)
    void updateUserEmailVerificationStatus(@Param("emailVerificationStatus") Boolean emailVerificationStatus,
                                                       @Param("userId") String userId);


    @Query("select user from UserEntity user where user.userId= :userId")
    UserEntity findUserEntityByUserId(@Param("userId") String userId);

    @Query("select user.firstName, user.lastName from UserEntity user where user.userId=:userId")
    List<Object[]> findUserEntityFullNameByUserId(@Param("userId") String userId);

    @Transactional
    @Modifying
    @Query("UPDATE UserEntity u set u.emailVerificationStatus=:emailVerificationStatus where u.userId=:userId")
    void updateUserEntityVerificationStatus(@Param("emailVerificationStatus") Boolean emailVerificationStatus,
                                           @Param("userId") String userId);

}
