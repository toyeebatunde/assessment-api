package co.payrail.nddc_assessment.integration.service;//package co.payrail.nddc_assessment.integration.service;
//
//import com.plethub.branchless.framework.app.setting.dto.SettingDTO;
//import com.plethub.branchless.framework.app.setting.service.ConfigurationService;
//import com.plethub.branchless.framework.app.user_management.model.PasswordHistory;
//import com.plethub.branchless.framework.app.user_management.model.User;
//import com.plethub.branchless.framework.app.user_management.repository.PasswordHistoryRepo;
//import com.plethub.branchless.framework.infrastructure.util.PasswordCreator;
//import com.plethub.branchless.framework.infrastructure.util.PasswordValidator;
//import org.apache.commons.lang3.math.NumberUtils;
//import org.hibernate.engine.config.spi.ConfigurationService;
//import org.joda.time.LocalDateTime;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.MessageSource;
//import org.springframework.context.i18n.LocaleContextHolder;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//
//@Service
//public class PasswordPolicyService {
//
//    private String ruleMessage = "";
//    @Autowired
//    private ConfigurationService configService;
//    @Autowired
//    private PasswordValidator passwordValidator;
//    @Autowired
//    private PasswordCreator passwordCreator;
//    @Autowired
//    private PasswordHistoryRepo adminPasswordRepo;
//
//    @Autowired
//    private MessageSource messageSource;
//
//    private List<String> passwordRules;
//    private SettingDTO numOfPasswordDigits;
//    private SettingDTO minLengthOfPassword;
//    private SettingDTO maxLengthOfPassword;
//    private SettingDTO noSpecialChar;
//    private SettingDTO specialChars;
//    private SettingDTO numOfChangesBeforeReuse;
//
//
//    private int numOfDigits = 0;
//    private int noOfSpecial = 0;
//    private int minLength = 8;
//    private int maxLength = 255;
//    private String specialCharacters = "@#$%&";
//    private int numOfChanges = 0;
//    private boolean initialized = false;
//
//    private Locale locale = LocaleContextHolder.getLocale();
//
//    private void init() {
//
//        passwordRules = new ArrayList<>();
//        numOfPasswordDigits = configService.getSettingByName("PASSWORD_NUM_DIGITS");
//        noSpecialChar = configService.getSettingByName("PASSWORD_NUM_SPECIAL_CHARS");
//        minLengthOfPassword = configService.getSettingByName("PASSWORD_MIN_LENGTH");
//        maxLengthOfPassword = configService.getSettingByName("PASSWORD_MAX_LENGTH");
//        specialChars = configService.getSettingByName("PASSWORD_SPECIAL_CHARS");
//        numOfChangesBeforeReuse = configService.getSettingByName("PASSWORD_REUSE");
//
//
//        if (numOfPasswordDigits != null && numOfPasswordDigits.isEnabled()) {
//            numOfDigits = NumberUtils.toInt(numOfPasswordDigits.getValue());
//
//            if (numOfDigits > 0) {
//                ruleMessage = String.format(messageSource.getMessage("pass.num.digit", null, locale), numOfDigits);
//                passwordRules.add(ruleMessage);
//            }
//
//        }
//        if (noSpecialChar != null && noSpecialChar.isEnabled()) {
//            noOfSpecial = NumberUtils.toInt(noSpecialChar.getValue());
//
//            if (noOfSpecial > 0) {
//                ruleMessage = String.format(messageSource.getMessage("pass.num.spec.char", null, locale), noOfSpecial);
//                passwordRules.add(ruleMessage);
//            }
//
//        }
//        if (minLengthOfPassword != null && minLengthOfPassword.isEnabled()) {
//            minLength = NumberUtils.toInt(minLengthOfPassword.getValue());
//            ruleMessage = String.format(messageSource.getMessage("pass.min.length", null, locale), minLength);
//            passwordRules.add(ruleMessage);
//
//        }
//        if (maxLengthOfPassword != null && maxLengthOfPassword.isEnabled()) {
//            maxLength = NumberUtils.toInt(maxLengthOfPassword.getValue());
//            ruleMessage = String.format(messageSource.getMessage("pass.max.length", null, locale), maxLength);
//            passwordRules.add(ruleMessage);
//        }
//        if (specialChars != null && specialChars.isEnabled()) {
//            specialCharacters = specialChars.getValue();
//            ruleMessage = String.format(messageSource.getMessage("pass.spec.char", null, locale), specialCharacters);
//            passwordRules.add(ruleMessage);
//
//        }
//        if (numOfChangesBeforeReuse != null && numOfChangesBeforeReuse.isEnabled()) {
//            numOfChanges = NumberUtils.toInt(numOfChangesBeforeReuse.getValue());
//            ruleMessage = String.format(messageSource.getMessage("pass.reuse", null, locale), numOfChanges);
//            passwordRules.add(ruleMessage);
//
//        }
//        initialized = true;
//
//    }
//
//    public String validate(String password, User user) {
//        return passwordValidator.validate(password, user);
//    }
//
//
//    public List<String> getPasswordRules() {
//        init();
//        return passwordRules;
//    }
//
//    public String generatePassword() {
//        init();
//        return passwordCreator.generatePassword(minLength, numOfDigits, specialCharacters);
//    }
//
//
//    public void saveAdminPassword(User adminUser) {
//
//        SettingDTO numOfChangesBeforeReuse = configService.getSettingByName("PASSWORD_REUSE");
//        if (numOfChangesBeforeReuse != null && numOfChangesBeforeReuse.isEnabled()) {
//            int count = adminPasswordRepo.countByUserId(adminUser.getId());
//            int numOfChanges = NumberUtils.toInt(numOfChangesBeforeReuse.getValue());
//
//            PasswordHistory adminPassword = new PasswordHistory();
//            adminPassword.setUserId(adminUser.getId());
//            adminPassword.setPassword(adminUser.getPassword());
//            if (numOfChanges > 0) {
//                if (count < numOfChanges) {
//                    adminPasswordRepo.save(adminPassword);
//                } else {
//                    PasswordHistory firstPassword = adminPasswordRepo.findFirstByUserId(adminUser.getId());
//                    adminPasswordRepo.delete(firstPassword);
//                    adminPasswordRepo.save(adminPassword);
//                }
//            }
//        }
//    }
//
//    public Date getPasswordExpiryDate() {
//        Calendar calendar = Calendar.getInstance();
//        SettingDTO setting = configService.getSettingByName("PASSWORD_EXPIRY");
//        if (setting != null && setting.isEnabled()) {
//            int days = NumberUtils.toInt(setting.getValue());
//            calendar.add(Calendar.DAY_OF_YEAR, days);
//            return calendar.getTime();
//        }
//        return null;
//    }
//
//
//    public boolean displayPasswordExpiryDate(Date expiryDate) {
//
//        if (expiryDate == null) {
//            return false;
//        }
//
//        SettingDTO setting = configService.getSettingByName("PASSWORD_AUTO_RESET");
//        if (setting != null && setting.isEnabled()) {
//
//            int days = NumberUtils.toInt(setting.getValue());
//            LocalDateTime dateToExpire = LocalDateTime.fromDateFields(expiryDate);
//            LocalDateTime dateToStartNotifying = dateToExpire.minusDays(days);
//            LocalDateTime now = LocalDateTime.now();
//
//            if (now.isAfter(dateToStartNotifying) && !now.isAfter(dateToExpire)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//}
