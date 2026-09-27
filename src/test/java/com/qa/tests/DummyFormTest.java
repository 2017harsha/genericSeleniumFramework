package com.qa.tests;

import com.qa.framework.base.BaseTest;
import com.qa.framework.config.ConfigManager;
import com.qa.pages.XPathPracticePage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import static com.qa.framework.base.ConstantPages.PASSWORD_INPUT_TYPE;
import static com.qa.framework.base.ConstantPages.PRACTICE_PAGE_TITLE;
import static com.qa.framework.base.ConstantTest.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Epic("XPath Practice Page")
@Feature("Dummy form")
public class DummyFormTest extends BaseTest {

    @Test(groups = {SMOKE, REGRESSION})
    @Severity(SeverityLevel.BLOCKER)
    @Description("Fills the form with the environment's credentials (prod: pulled from AWS Parameter Store)")
    public void fillFormWithEnvironmentCredentials() {
        String user = ConfigManager.getRequired(KEY_APP_USERNAME);
        String pass = ConfigManager.getRequired(KEY_APP_PASSWORD);

        XPathPracticePage page = new XPathPracticePage().open()
                .fillDummyForm(user, pass, COMPANY_NAME, MOBILE_NUMBER);

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(page.emailValue(), user, "email");
        soft.assertEquals(page.passwordValue(), pass, "password");
        soft.assertEquals(page.companyValue(), COMPANY_NAME, "company");
        soft.assertEquals(page.mobileValue(), MOBILE_NUMBER, "mobile");
        soft.assertEquals(page.passwordFieldType(), PASSWORD_INPUT_TYPE, "password should be masked");
        soft.assertTrue(page.isSubmitDisplayed(), "submit button visible");
        soft.assertAll();
    }

    @DataProvider(name = DP_FORM_DATA, parallel = true)
    public Object[][] formData() {
        return FORM_DATA.clone();
    }

    @Test(dataProvider = DP_FORM_DATA, groups = REGRESSION)
    @Severity(SeverityLevel.NORMAL)
    @Description("Data-driven form fill; rows run in parallel threads")
    public void fillFormDataDriven(String email, String password, String company, String mobile) {
        XPathPracticePage page = new XPathPracticePage().open()
                .fillDummyForm(email, password, company, mobile)
                .submitForm();

        assertEquals(page.emailValue(), email);
        assertEquals(page.companyValue(), company);
    }

    @Test(groups = REGRESSION)
    @Severity(SeverityLevel.MINOR)
    public void typeInFirstCrushField() {
        XPathPracticePage page = new XPathPracticePage().open().typeFirstCrush(FIRST_CRUSH_NAME);
        assertEquals(page.firstCrushValue(), FIRST_CRUSH_NAME);
        assertTrue(page.getTitle().contains(PRACTICE_PAGE_TITLE), "page title");
    }
}
