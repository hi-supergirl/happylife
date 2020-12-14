package com.happylife.core.dto.user;

import com.happylife.core.exception.user.UserFilterParameterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class UserFilter {
    private String userIds;
    private String name;
    private String sex;
    private String sortby;
    private String order;

    @Autowired
    private MessageSource messageSource;

    public UserFilter(){}

    public String getUserIds() {
        return userIds;
    }

    public void setUserIds(String userIds) {
        this.userIds = userIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSortby() {
        return sortby;
    }

    public void setSortby(String sortby) {
        this.sortby = sortby;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    private void validateUserIds(){
        String[] idArr = this.userIds.split(",");
        boolean isValid = true;
        StringBuffer reason = new StringBuffer();
        reason.append("for userIds, invalid ids are:");
        for(String id : idArr){
            try{
                UUID.fromString(id);
            }catch (IllegalArgumentException ex){
                isValid = false;
                if(!invalidFields.contains("userIds")){
                    invalidFields.add("userIds");
                }
                reason.append(id);
                reason.append(",");
            }
        }
        if(!isValid){
            reasons.add(reason.toString());
        }
        return;
    }

    private List<String> invalidFields = new ArrayList<String>();
    private List<String> reasons = new ArrayList<String>();

    private void validateName(){
        return;
    }

    private void validateSex(){
        return;
    }

    private void validateSortby(){
        return;
    }

    private void validateOrder(){
        return;
    }

    public void validate() throws UserFilterParameterException {
        validateUserIds();
        validateName();
        validateSex();
        validateSortby();
        validateOrder();
        if(invalidFields.size() > 1){
            throw new UserFilterParameterException(this.messageSource.getMessage("user.filter.validation", new Object[]{invalidFields.toString(), reasons.toString()}, Locale.getDefault()));
        }
    }

    @Override
    public String toString() {
        return "UserFilter{" +
                "userIds='" + userIds + '\'' +
                ", name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", sortby='" + sortby + '\'' +
                ", order='" + order + '\'' +
                '}';
    }
}
