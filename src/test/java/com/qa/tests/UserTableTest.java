package com.qa.tests;

import com.qa.framework.base.BaseTest;
import com.qa.pages.XPathPracticePage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.qa.framework.base.ConstantPages.*;
import static com.qa.framework.base.ConstantTest.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Epic("XPath Practice Page")
@Feature("User table (XPath axes)")
public class UserTableTest extends BaseTest {

    @Test(groups = {SMOKE, REGRESSION})
    @Severity(SeverityLevel.CRITICAL)
    public void tableHasSixUsers() {
        assertEquals(new XPathPracticePage().open().userRowCount(), EXPECTED_USER_COUNT);
    }

    @Test(groups = REGRESSION)
    @Severity(SeverityLevel.NORMAL)
    public void selectUserCheckboxUsingXpathAxes() {
        XPathPracticePage page = new XPathPracticePage().open();
        assertFalse(page.isUserSelected(USER_JOHN_SMITH), "precondition: unchecked");
        page.selectUser(USER_JOHN_SMITH);
        assertTrue(page.isUserSelected(USER_JOHN_SMITH), USER_JOHN_SMITH + " should be checked");
        assertFalse(page.isUserSelected(USER_JOE_ROOT), "other rows untouched");
    }

    @DataProvider(name = DP_USERS)
    public Object[][] users() {
        return USERS.clone();
    }

    @Test(dataProvider = DP_USERS, groups = REGRESSION)
    @Severity(SeverityLevel.NORMAL)
    public void verifyUserDetails(String username, String role, String employeeName) {
        XPathPracticePage page = new XPathPracticePage().open();
        assertEquals(page.userCell(username, COL_USER_ROLE), role, "role");
        assertEquals(page.userCell(username, COL_EMPLOYEE_NAME), employeeName, "employee name");
        assertEquals(page.userCell(username, COL_STATUS), USER_STATUS_ENABLED, "status");
    }
}
