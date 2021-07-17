package net.tomobou.extensions;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonNodeTransformUtilTest {

    @ParameterizedTest
    @CsvSource({
            "REPLACE, '{\"k1\":\"v1\"}',k1,30,'{\"k1\":\"30\"}'",
            "ADD, '{\"k1\":\"v1\"}',k2,30,'{\"k1\":\"v1\",\"k2\":\"30\"}'",
            "ADD_INTEGER_VALUE, '{\"k1\":\"v1\"}',k2,!INTEGER!30,'{\"k1\":\"v1\",\"k2\":30}'",
            "ADD_DOUBLE_VALUE, '{\"k1\":\"v1\"}',k2,!DOUBLE!30.5,'{\"k1\":\"v1\",\"k2\":30.5}'",
            "DELETE_VALUE, '{\"k1\":\"v1\",\"k2\":30}',k2,!DELETE!,'{\"k1\":\"v1\"}'",
            "DELETE_CHILD_VALUE, '{\"k2\":{\"kc1\":\"v1\",\"kc2\":\"v2\"},\"k1\":{\"kc1\":\"v1\",\"kc2\":\"v2\"}}',k2.kc2,!DELETE!,'{\"k2\":{\"kc1\":\"v1\"},\"k1\":{\"kc1\":\"v1\",\"kc2\":\"v2\"}}'",
            "ARRAY, '{\"k2\":{\"kc1\":\"v1\",\"kc2\":\"v2\"},\"k1\":{\"kc1\":\"v1\",\"kc2\":\"v2\"}}',k2.kc3,!ARRAY!3,'{\"k2\":{\"kc1\":\"v1\",\"kc2\":\"v2\",\"kc3\":[{},{},{}]},\"k1\":{\"kc1\":\"v1\",\"kc2\":\"v2\"}}'",
            "ADD_TO_ARRAY, '{\"k2\":{\"kc1\":\"v1\",\"kc2\":\"v2\",\"kc3\":[{},{},{}]}}',k2.kc3.kcc1,testdata,'{\"k2\":{\"kc1\":\"v1\",\"kc2\":\"v2\",\"kc3\":[{\"kcc1\":\"testdata\"},{\"kcc1\":\"testdata\"},{\"kcc1\":\"testdata\"}]}}'"
    })
    void nodeTransform_success(String testName, String json, String path, String operation, String required) throws JsonProcessingException {
        JsonNode node = new ObjectMapper().readTree(json);
        List<String> splitPath = Arrays.asList(path.split("\\."));

        JsonNode actual = JsonNodeTransformUtil.nodeTransform(node, splitPath, operation);
        assertEquals(required, actual.toString(), testName);

    }
}