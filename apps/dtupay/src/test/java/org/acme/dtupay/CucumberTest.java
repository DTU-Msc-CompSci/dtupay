package org.acme.dtupay;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.CucumberOptions.SnippetType;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin="summary"
        //, publish= false
        , features = "features"  // directory of the feature files
        , snippets = SnippetType.CAMELCASE
)
public class CucumberTest {
}
