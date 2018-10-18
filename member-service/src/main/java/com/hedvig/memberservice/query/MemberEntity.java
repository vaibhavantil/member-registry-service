package com.hedvig.memberservice.query;

import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumMap;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.hedvig.memberservice.aggregates.MemberStatus;
import com.hedvig.memberservice.util.EnumMapChecker;
import com.hedvig.memberservice.web.dto.MembersSortColumn;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;

@Entity
@Table(
    name = "member_entity",
    indexes = {@Index(columnList = "ssn", name = "ix_member_entity_ssn")})
@Getter
@Setter
public class MemberEntity {

  @Id private Long id;

  String zipCode;
  private String apartment;

  @Enumerated(EnumType.STRING)
  private MemberStatus status;
  private String ssn;
  private String firstName;
  private String lastName;
  private LocalDate birthDate;
  private String street;
  private String city;

  private String phoneNumber;

  private String email;

  private String cashbackId;

  private Integer floor;

  private Instant signedOn;

  private Instant createdOn;

  @Formula("concat(last_name, first_name)")
  private String fullName;

  public static final EnumMap<MembersSortColumn, String> SORT_COLUMN_MAPPING = new EnumMap<MembersSortColumn, String>(MembersSortColumn.class) {{
    put(MembersSortColumn.CREATED, "createdOn");
    put(MembersSortColumn.NAME, "fullName");
    put(MembersSortColumn.SIGN_UP, "signedOn");

    EnumMapChecker.ensureMapContainsAllEnumVals(this, MembersSortColumn.class);
  }};
}
