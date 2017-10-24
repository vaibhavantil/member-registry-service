import com.hedvig.external.billectaAPI.api.Debtor;
import com.hedvig.external.billectaAPI.api.DebtorAutogiro;
import com.hedvig.external.billectaAPI.api.DebtorType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class DebtorTest {

    @Test
    public void TestCreate(){
        Debtor deb = new Debtor();
        deb.setDebtorExternalId("23445");
        deb.setAddress("Långgatan 3");
        deb.setCity("Täby");
        deb.setZipCode("18774");
        deb.setDebtorType(DebtorType.PRIVATE);
        deb.setCountryCode("se");
        deb.setEmail("member@email.com");
        deb.setContactEmail("kundtjant@hedvig.com");

        DebtorAutogiro autogiro = new DebtorAutogiro();
        autogiro.setAccountNo("8881882");
        autogiro.setClearingNo("Clearing no");
        autogiro.setPaymentServiceSupplier("SWEBANK");

        deb.setAutogiro(autogiro);

    }

}
