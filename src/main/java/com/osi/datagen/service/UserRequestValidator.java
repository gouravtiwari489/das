package com.osi.datagen.service;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.osi.datagen.domain.CustomUserDetails;
import com.osi.datagen.domain.TableList;
@Component
public class UserRequestValidator {

  @Autowired
  public LogoutService logoutService;

  @Value("${file.download.path}")
  public String fileDownloadPath;

  @Value("${file.types}")
  public String[] fileTypes;

  

  public boolean IsSameRequestPresnt(String fileType, TableList tables, int rowCount) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

    if (user.getMap() == null || !user.getMap().containsKey(fileType)
        || user.getMappedData() == null || !user.getMappedData().equals(tables)
        || user.getMap().get(fileType) != rowCount) {
      logoutService.clearPreExistingUserData(fileDownloadPath, tables, user,
          Arrays.asList(fileTypes));
       return false;
    }
    return true;

  }
}
