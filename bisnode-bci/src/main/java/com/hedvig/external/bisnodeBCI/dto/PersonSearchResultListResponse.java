package com.hedvig.external.bisnodeBCI.dto;

import lombok.Value;
import java.util.List;

@Value
public class PersonSearchResultListResponse {
    private List<PersonSearchResult> persons;
}
