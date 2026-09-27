package com.qa.tests;

import com.qa.framework.base.BaseTest;
import com.qa.pages.XPathPracticePage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import java.util.List;

import static com.qa.framework.base.ConstantPages.DATATABLE_FILTERED_TEXT;
import static com.qa.framework.base.ConstantPages.DATATABLE_INFO_PREFIX;
import static com.qa.framework.base.ConstantTest.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Epic("XPath Practice Page")
@Feature("Dynamic data table")
public class DataTableTest extends BaseTest {

    @Test(groups = REGRESSION)
    @Severity(SeverityLevel.CRITICAL)
    public void searchFiltersRows() {
        XPathPracticePage page = new XPathPracticePage().open().searchDataTable(DATATABLE_SEARCH_TERM);

        List<String> rows = page.dataTableRows();
        assertFalse(rows.isEmpty(), "search should return rows");
        rows.forEach(r -> assertTrue(r.contains(DATATABLE_SEARCH_TERM), "row does not match filter: " + r));
        assertTrue(page.dataTableInfo().contains(DATATABLE_FILTERED_TEXT), "info text: " + page.dataTableInfo());
    }

    @Test(groups = REGRESSION)
    @Severity(SeverityLevel.NORMAL)
    public void changePageLength() {
        XPathPracticePage page = new XPathPracticePage().open();
        assertEquals(page.dataTableRows().size(), DATATABLE_DEFAULT_PAGE_SIZE, "default page size");
        page.setDataTablePageLength(DATATABLE_PAGE_SIZE_25);
        assertEquals(page.dataTableRows().size(), DATATABLE_EXPECTED_ROWS_25, "rows after choosing 25");
        assertTrue(page.dataTableInfo().startsWith(DATATABLE_INFO_PREFIX + DATATABLE_EXPECTED_ROWS_25),
                page.dataTableInfo());
    }
}
