package com.hedvig.memberservice.web.dto;

import com.hedvig.memberservice.aggregates.PickedLocale;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickedLocaleDTO {

  private PickedLocale pickedLocale;

}
