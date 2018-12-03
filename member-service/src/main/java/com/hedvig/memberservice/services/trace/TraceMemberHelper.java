package com.hedvig.memberservice.services.trace;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.memberservice.events.Traceable;
import com.hedvig.memberservice.web.dto.TraceMemberDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.jwt.JwtHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
public class TraceMemberHelper {

  public static String formatDate (Object date) {
    if (date != null) {
      if (date instanceof LocalDateTime) return formatDate(localDateToInstant((LocalDateTime)date));
      if (date instanceof Instant) return formatDate((Instant) date);

      return date.toString();

    }
    return "";
  }

  public static Instant localDateToInstant (LocalDateTime date) {
    return date.atZone(ZoneId.of("Europe/Stockholm")).toInstant();
  }

  private static String formatDate (Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM YYYY");
    return sdf.format(date);
  }

  private static String formatDate (Instant date) {
    return formatDate(Date.from(date));
  }

  public static String getIxMail(String token) {
    try {
      Map<String, String> map = (new ObjectMapper().readValue(JwtHelper.decode(token).getClaims(), new TypeReference<Map<String, String>>() {}));
      return map.get("email");
    } catch (IOException e) {
      log.error(e.getMessage());
      return null;
    } catch (IllegalArgumentException e) {
      log.error(e.getMessage());
      return "";
    }
  }

  public static Map<String, TraceMemberDTO> getTraceInfo (Traceable entity, String memberId, Instant changeDate, Object token) {
    final Map<String, TraceMemberDTO> result = new HashMap<>();
    final String email =  TraceMemberHelper.getIxMail (Optional.ofNullable(token).orElse("").toString());

    entity.getValues().keySet().forEach((String item) -> result.put(formatDate (entity.getValues().get(item))+item+email, new TraceMemberDTO (
      LocalDateTime.ofInstant (changeDate, ZoneId.of("Europe/Stockholm")),
      "", formatDate (entity.getValues().get(item)),
      item, memberId, email)));

    return result;
  }

}
