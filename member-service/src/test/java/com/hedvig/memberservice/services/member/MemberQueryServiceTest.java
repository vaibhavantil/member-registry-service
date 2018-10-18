package com.hedvig.memberservice.services.member;

import com.hedvig.memberservice.TestApplication;
import com.hedvig.memberservice.aggregates.MemberStatus;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.web.dto.InternalMemberSearchRequestDTO;
import com.hedvig.memberservice.web.dto.InternalMemberSearchResultDTO;
import com.hedvig.memberservice.web.dto.MembersSortColumn;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import static com.hedvig.memberservice.aggregates.MemberStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestApplication.class)
@DataJpaTest(includeFilters =
  @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = MemberQueryService.class)
)
public class MemberQueryServiceTest {

  @Autowired
  TestEntityManager entityManager;

  @Autowired
  MemberQueryService memberQueryService;


  private void init() {
    createMember(10001, "John", "Doe", TERMINATED, "23-09-2018 14:12:01", b ->
      b.signedOn(parseToInstant("23-09-2018 23:41:25"))
    );
    createMember(10002, "Lois", "Ester", INITIATED, "23-09-2018 15:13:02");
    createMember(10003, "Janna", "Kristen", INITIATED, "23-09-2018 15:17:22");
    createMember(10004, "Henrik", "Ester", SIGNED, "23-09-2018 17:45:25", b ->
      b.signedOn(parseToInstant("23-09-2018 21:43:25"))
    );
    createMember(10005, "Coty", "Oneida", INACTIVATED, "24-09-2018 10:09:53", b ->
      b.signedOn(parseToInstant("25-09-2018 09:10:14"))
    );
    createMember(10006, "Henrik", "Eldred", ONBOARDING, "24-09-2018 11:48:04");
  }

  @Test
  public void nullPageGivesUnpagedSearch() {
    init();
    InternalMemberSearchResultDTO res = search("", null, null, 2, null, null);
    assertThat(res.getPage()).isNull();
    assertThat(res.getTotalPages()).isNull();
    assertThat(res.getMembers().size()).isEqualTo(6);
  }

  @Test
  public void nullPageSizeGivesUnpagedSearch() {
    init();
    InternalMemberSearchResultDTO res = search("", null, 1, null, null, null);
    assertThat(res.getPage()).isNull();
    assertThat(res.getTotalPages()).isNull();
    assertThat(res.getMembers().size()).isEqualTo(6);
  }

  @Test
  public void unpagedSortByNameAsc() {
    init();
    InternalMemberSearchResultDTO res = search("", null, null, null, MembersSortColumn.NAME, Sort.Direction.ASC);
    assertThat(res.getMembers().size()).isEqualTo(6);
    assertThat(res.getMembers().get(0).getLastName()).isEqualTo("Doe");
    assertThat(res.getMembers().get(5).getLastName()).isEqualTo("Oneida");
  }

  @Test
  public void unpagedSortByNameDesc() {
    init();
    InternalMemberSearchResultDTO res = search("", null, null, null, MembersSortColumn.NAME, Sort.Direction.DESC);
    assertThat(res.getMembers().size()).isEqualTo(6);
    assertThat(res.getMembers().get(0).getLastName()).isEqualTo("Oneida");
    assertThat(res.getMembers().get(5).getLastName()).isEqualTo("Doe");
  }

  @Test
  public void unpagedSortBySignUpAsc() {
    init();
    InternalMemberSearchResultDTO res = search("", null, null, null, MembersSortColumn.SIGN_UP, Sort.Direction.ASC);
    assertThat(res.getMembers().size()).isEqualTo(6);
    assertThat(dateFromInstant(res.getMembers().get(0).getSignedOn())).isEqualTo("23-09-2018 21:43:25");
    assertThat(dateFromInstant(res.getMembers().get(2).getSignedOn())).isEqualTo("25-09-2018 09:10:14");
    assertThat(dateFromInstant(res.getMembers().get(3).getSignedOn())).isNull();
  }

  @Test
  public void unpagedSortBySignUpDesc() {
    init();
    InternalMemberSearchResultDTO res = search("", null, null, null, MembersSortColumn.SIGN_UP, Sort.Direction.DESC);
    assertThat(res.getMembers().size()).isEqualTo(6);
    assertThat(dateFromInstant(res.getMembers().get(0).getSignedOn())).isEqualTo("25-09-2018 09:10:14");
    assertThat(dateFromInstant(res.getMembers().get(2).getSignedOn())).isEqualTo("23-09-2018 21:43:25");
    assertThat(dateFromInstant(res.getMembers().get(3).getSignedOn())).isNull();
  }

  @Test
  public void pagedSortByNameDesc() {
    init();
    InternalMemberSearchResultDTO res = search(null, null, 1, 3, MembersSortColumn.NAME, Sort.Direction.DESC);
    assertThat(res.getMembers().size()).isEqualTo(3);
    assertThat(res.getMembers().get(0).getLastName()).isEqualTo("Ester");
    assertThat(res.getMembers().get(1).getLastName()).isEqualTo("Eldred");
    assertThat(res.getMembers().get(2).getLastName()).isEqualTo("Doe");
    assertThat(res.getTotalPages()).isEqualTo(2);
  }

  public void unpagedFirstNameQuerySortedByNameDesc() {
    init();
    InternalMemberSearchResultDTO res = search("Henrik", null, null, null, MembersSortColumn.NAME, Sort.Direction.DESC);
    assertThat(res.getMembers().size()).isEqualTo(2);
    assertThat(res.getMembers().get(0).getLastName()).isEqualTo("Ester");
    assertThat(res.getMembers().get(1).getLastName()).isEqualTo("Eldred");
  }

  public void unpagedLastNameQuerySortedByNameDesc() {
    init();
    InternalMemberSearchResultDTO res = search("Ester", null, null, null, MembersSortColumn.NAME, Sort.Direction.DESC);
    assertThat(res.getMembers().size()).isEqualTo(2);
    assertThat(res.getMembers().get(0).getFirstName()).isEqualTo("Lois");
    assertThat(res.getMembers().get(1).getFirstName()).isEqualTo("Henrik");
  }

  public void unpagedMemberIdQuerySortedByNameDesc() {
    init();
    InternalMemberSearchResultDTO res = search("10003", null, null, null, MembersSortColumn.NAME, Sort.Direction.DESC);
    assertThat(res.getMembers().size()).isEqualTo(1);
    assertThat(res.getMembers().get(0).getMemberId()).isEqualTo(10003);
  }

  public void unpagedInitiatedStatusSortedByCreatedDesc() {
    init();
    InternalMemberSearchResultDTO res = search(null, INITIATED, null, null, MembersSortColumn.CREATED, Sort.Direction.DESC);
    assertThat(res.getMembers().size()).isEqualTo(1);
    assertThat(res.getMembers().get(0).getMemberId()).isEqualTo(10003);
    assertThat(res.getMembers().get(1).getMemberId()).isEqualTo(10002);
  }

  private InternalMemberSearchResultDTO search(String query, MemberStatus status, Integer page, Integer pageSize, MembersSortColumn sortBy, Sort.Direction sortDir) {
    return memberQueryService.search(new InternalMemberSearchRequestDTO(query, status, page, pageSize, sortBy, sortDir));
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
