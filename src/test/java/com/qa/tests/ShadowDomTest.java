package com.qa.tests;

import com.qa.framework.base.BaseTest;
import com.qa.pages.XPathPracticePage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import static com.qa.framework.base.ConstantTest.*;
import static org.testng.Assert.assertEquals;

@Epic("XPath Practice Page")
@Feature("Shadow DOM")
public class ShadowDomTest extends BaseTest {

    @Test(groups = {SMOKE, REGRESSION})
    @Severity(SeverityLevel.CRITICAL)
    public void typeInsideShadowRoot() {
        XPathPracticePage page = new XPathPracticePage().open().typeInShadowUsername(SHADOW_USERNAME);
        assertEquals(page.shadowUsernameValue(), SHADOW_USERNAME);
    }

    @Test(groups = REGRESSION)
    @Severity(SeverityLevel.NORMAL)
    public void typeInsideNestedShadowRoot() {
        XPathPracticePage page = new XPathPracticePage().open().typeInNestedShadowPizza(SHADOW_PIZZA);
        assertEquals(page.nestedShadowPizzaValue(), SHADOW_PIZZA);
    }
}
