package com.happylife.core.controller.user;

import com.happylife.core.common.Response;
import com.happylife.core.common.UUIDGenerator;
import com.happylife.core.dto.user.UserFilter;
import com.happylife.core.exception.EntityNotFoundException;
import com.happylife.core.exception.user.UserException;
import com.happylife.core.exception.user.UserFilterParameterException;
import com.happylife.core.exception.uuid.UUIDException;
import com.happylife.core.mbg.model.Student;
import com.happylife.core.mbg.model.User;
import com.happylife.core.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;


@RestController
@RequestMapping("/tuoke-web/api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private MessageSource messageSource;
    @Autowired
    private UUIDGenerator uuidGenerator;

    @Autowired
    private UserService userService;

    /**
     * This is a demo to show how to use messageSource
     * @param name
     * @return
     */
    @GetMapping(value = "/test")
    public ResponseEntity<Object> test(@RequestParam(value = "name", required = true) String name){
        logger.info(messageSource.getMessage("subject", new Object[]{"juan"}, Locale.getDefault()));
        return new ResponseEntity<>("hello", HttpStatus.OK);
    }

    /**
     * This is a demo to show how to use @Valid and
     * However, the method studentVerify doesn't provide good solution to collect exceptions/errors thrown by @Valid
     * If we do nothing about collecting exceptions/errors, spring will throw exceptions/errors in backend(you will see it on spring console/log file)
     * Currently, there are several ways to collect exceptions/errors
     *   1. Add anther parameter which type is BindingResult in studentVerify, then use AOP or directly return errors within studentVerify.
     *      For AOP, see details: http://www.macrozheng.com/#/technology/springboot_validator
     *      For direct return, see details: https://blog.csdn.net/sunnyzyq/article/details/103527380
     *   2. Use annotation @ExceptionHandler on the controller-specific level or global level.
     *      See details at handleValidationExceptions method (controller-specific level) or at {@link com.happylife.core.exception.GlobalExceptionHandler}(global level)
     * @param student
     * @return
     */
    @PostMapping(value = "/student")
    public ResponseEntity<Object> studentVerify(@Valid @RequestBody Student student){
        return new ResponseEntity<>("student is valid", HttpStatus.OK);
    }

    /**
     * we could handle controller-specific exceptions by using the method below.
     * For the common exceptions, put the handling logic at here. See {@link com.happylife.core.exception.GlobalExceptionHandler}
     * @param ex
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }


    @GetMapping
    public ResponseEntity<Object> getUsersByFilter(@RequestParam(value = "userIds", required = false, defaultValue = "") String userIds,
                                                   @RequestParam(value = "name", required = false, defaultValue = "") String name,
                                                   @RequestParam(value = "sex", required = false, defaultValue = "") String sex,
                                                   @RequestParam(value = "sortby", required = false, defaultValue = "") String sortby,
                                                   @RequestParam(value = "order", required = false, defaultValue = "") String order) throws UserFilterParameterException, UserException {
        UserFilter userFilter = new UserFilter(this.messageSource);
        userFilter.setUserIds(userIds);
        userFilter.setName(name);
        userFilter.setSex(sex);
        userFilter.setSortby(sortby);
        userFilter.setOrder(order);
        userFilter.validate();
        logger.info(userFilter.toString());
        List<User> users = null;
        try{
            users = userService.getUsersByFilter(userFilter);
        }catch(UserException ex){
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
        logger.info(this.messageSource.getMessage("user.filter", new Object[]{userFilter.toString()}, Locale.getDefault()));
        Response response = Response.success(users);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable("userId") String userId) throws UUIDException, EntityNotFoundException, UserException {
        uuidGenerator.validate(userId, "userId", "User");
        UUID uuid = uuidGenerator.getUUID(userId);
        User user = null;
        try{
            user = userService.getUserById(uuid);
        }catch(UserException ex){
            logger.error(ex.getMessage(), ex);
            throw ex;
        }

        if(user == null){
            throw new EntityNotFoundException(this.messageSource.getMessage("entity.notfound", new Object[]{uuid.toString(), "user"}, Locale.getDefault()));
        }
        logger.info(this.messageSource.getMessage("user.filter.id", new Object[]{userId}, Locale.getDefault()));
        Response response = Response.success(user);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteUsersByIds(@RequestParam(value = "userIds", required = true) String userIds) throws UUIDException, UserException {
        //to-do: validation for userIds
        List<Object> uuids = uuidGenerator.getUUIDs(userIds);
        try{
            int res = userService.deleteUsersByIds(uuids);
            logger.info(this.messageSource.getMessage("user.delete.res", new Object[]{res}, Locale.getDefault()));
            logger.info(this.messageSource.getMessage("user.delete.ids", new Object[]{userIds}, Locale.getDefault()));
        }catch(Exception ex){
            logger.error(ex.getMessage(), ex);
            throw new UserException(ex.getMessage());
        }

        Response response = Response.success();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping(value = "/{userId}")
    public ResponseEntity<Object> deleteUserById(@PathVariable("userId") String userId) throws UUIDException, UserException {
        uuidGenerator.validate(userId, "userId", "User");
        UUID uuid = uuidGenerator.getUUID(userId);
        try{
            int res = userService.deleteUserById(uuid);
            logger.info(this.messageSource.getMessage("user.delete.res", new Object[]{res}, Locale.getDefault()));
            logger.info(this.messageSource.getMessage("user.delete.ids", new Object[]{userId}, Locale.getDefault()));
        }catch(Exception ex){
            logger.error(ex.getMessage(), ex); // print exception stack
            throw new UserException(ex.getMessage());
        }

        Response response = Response.success();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody User user) throws UserException {
        UUID uuid = uuidGenerator.getUUID();
        user.setUserId(uuid);
        try{
            int res = userService.createUser(user);
            logger.info(this.messageSource.getMessage("user.create.res", new Object[]{res}, Locale.getDefault()));
        }catch(UserException ex){
            logger.error(ex.getMessage(), ex);
            throw ex;
        }

        Response response = Response.success(user);
        logger.info(this.messageSource.getMessage("user.create", new Object[]{uuid.toString()}, Locale.getDefault()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private User checkUser(UUID uuid) throws EntityNotFoundException, UserException {
        User user = userService.getUserById(uuid);
        if(user == null)
            throw new EntityNotFoundException(this.messageSource.getMessage("entity.notfound", new Object[]{uuid.toString(), "user"}, Locale.getDefault()));
        return user;
    }

    @PutMapping
    public ResponseEntity<Object> updateUser(@RequestBody User user) throws EntityNotFoundException, UUIDException, UserException {
        uuidGenerator.validate(user.getUserId() == null? null : user.getUserId().toString(), "userId", "User");
        UUID uuid = uuidGenerator.getUUID(user.getUserId().toString());
        user.setUserId(uuid);
        checkUser(uuid);
        try{
            int res = userService.updateUser(user);
            logger.info(this.messageSource.getMessage("user.update.res", new Object[]{res}, Locale.getDefault()));
        }catch(UserException ex){
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
        Response response = Response.success(user);
        logger.info(this.messageSource.getMessage("user.update", new Object[]{uuid.toString()}, Locale.getDefault()));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
