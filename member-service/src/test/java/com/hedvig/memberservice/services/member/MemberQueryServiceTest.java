package com.hedvig.memberservice.services.member;

import com.hedvig.memberservice.aggregates.MemberStatus;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.web.dto.InternalMemberSearchRequestDTO;
import com.hedvig.memberservice.web.dto.InternalMemberSearchResultDTO;
import com.hedvig.memberservice.web.dto.MembersSortColumn;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import static com.hedvig.memberservice.aggregates.MemberStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest(includeFilters =
  @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = MemberQueryService.class)
)
public class MemberQueryServiceTest {

  @Autowired
  TestEntityManager entityManager;

  @Autowired
  MemberQueryService memberQueryService;

  @Before
  public void init() {
    createMember(10001, "John", "Doe", TERMINATED, "23-09-2018 14:12:01", b ->
      b.signedOn(parseToInstant("23-09-2018 23:41:25"))
      .ssn("20121212-1212")
    );
    createMember(10002, "Lois", "Ester", INITIATED, "23-09-2018 15:13:02", b ->
      b.email("louis.ester@hedvig.com")
    );
    createMember(10003, "Janna", "Kristen", INITIATED, "23-09-2018 15:17:22");
    createMember(10004, "Henrik", "Ester", SIGNED, "23-09-2018 17:45:25", b ->
      b.signedOn(parseToInstant("23-09-2018 21:43:25"))
        .email("henrik.ester@hedvig.com")
        .ssn("19010101-0101")
    );
    createMember(10005, "Coty", "Oneida", INACTIVATED, "24-09-2018 10:09:53", b ->
      b.signedOn(parseToInstant("25-09-2018 09:10:14"))
    );
    createMember(10006, "Henrik", "Eldred", ONBOARDING, "24-09-2018 11:48:04");
  }

  @Test
  public void emptyQueryGivesEmptyResult() {
    InternalMemberSearchResultDTO res = search("", true, null, 2, null, null);
    assertThat(res.getPage()).isNull();
    assertThat(res.getTotalPages()).isNull();
    assertThat(res.getMembers().size()).isEqualTo(0);
  }

  @Test
  public void searchesByLastName() {
    InternalMemberSearchResultDTO res = search("Ester", true, null, null, MembersSortColumn.NAME, null);
    assertThat(res.getMembers().size()).isEqualTo(2);
    assertThat(res.getMembers().get(0).getFirstName()).isEqualTo("Henrik");
    assertThat(res.getMembers().get(1).getFirstName()).isEqualTo("Lois");
  }

  @Test
  public void searchesByFirstName() {
    InternalMemberSearchResultDTO res = search("Henrik", true, null, null, null, null);
    assertThat(res.getMembers().size()).isEqualTo(2);
    assertThat(res.getMembers().get(0).getLastName()).isEqualTo("Ester");
    assertThat(res.getMembers().get(1).getLastName()).isEqualTo("Eldred");
  }

  @Test
  public void searchesMemberId() {
    InternalMemberSearchResultDTO res = search("10003", true, null, null, null, null);
    assertThat(res.getMembers().size()).isEqualTo(1);
    assertThat(res.getMembers().get(0).getMemberId()).isEqualTo(10003);
  }

  @Test
  public void searchesBySsn() {
    InternalMemberSearchResultDTO result = search("19010101-0101", null, null, null, null, null);
    assertThat(result.getMembers()).hasSize(1);
    assertThat(result.getMembers().get(0).getMemberId()).isEqualTo(10004);
  }

  @Test
  public void searchesByEmail() {
    InternalMemberSearchResultDTO result = search("louis.ester@hedvig.com", true, null, null, null, null);
    assertThat(result.getMembers()).hasSize(1);
    assertThat(result.getMembers().get(0).getMemberId()).isEqualTo(10002);
  }


  @Test
  public void searchesLikeEmail() {
    InternalMemberSearchResultDTO result = search("ester@hedvig.com", true, null, null, null, null);
    assertThat(result.getMembers()).hasSize(2);
  }


  private InternalMemberSearchResultDTO search(String query, Boolean searchAll, Integer page, Integer pageSize, MembersSortColumn sortBy, Sort.Direction sortDir) {
    return memberQueryService.search(new InternalMemberSearchRequestDTO(query, searchAll, page, pageSize, sortBy, sortDir));
  }

  static MemberEntity.MemberEntityBuilder memberBuilder(long id, String firstName, String lastName, MemberStatus status, String createdOn) {
    return MemberEntity.builder()
      .id(id)
      .status(MemberStatus.INITIATED)
      .firstName(firstName)
      .lastName(lastName)
      .status(status)
      .createdOn(parseToInstant(createdOn));
  }

  private void createMember(long id, String firstName, String lastName, MemberStatus status, String createdOn, Function<MemberEntity.MemberEntityBuilder, MemberEntity.MemberEntityBuilder> buildFunc) {
    MemberEntity.MemberEntityBuilder builder = memberBuilder(id, firstName, lastName, status, createdOn);
    if (buildFunc != null) {
      builder = buildFunc.apply(builder);
    }
    entityManager.persist(builder.build());
  }

  private void createMember(long id, String firstName, String lastName, MemberStatus status, String createdOn) {
    createMember(id, firstName, lastName, status, createdOn,null);
  }

  private static Instant parseToInstant(String date) {
    return LocalDateTime.from(dateFmt.parse(date)).atZone(ZoneId.of("UTC")).toInstant();
  }

  private static String dateFromInstant(Instant in) {
    if (in == null) {
      return null;
    }
    return dateFmt.format(in.atZone(ZoneId.of("UTC")).toLocalDateTime());
  }

  static final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
}
