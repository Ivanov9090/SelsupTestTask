import ru.ivanov9090.CrptApi;

/*
Тесты написанного решения, отметающие наиболее вероятные ошибки
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class CrptApiTest {

    private CrptApi crptApi;
    private CrptApi.Document document;

    // Создадим тестовый документ
    @BeforeEach
    public void setUp() {
        crptApi = new CrptApi(TimeUnit.SECONDS, 5);
        document = new CrptApi.Document();
        document.description = new CrptApi.Document.Description();
        document.description.participantInn = "4";
        document.doc_id = "8";
        document.doc_status = "new";
        document.doc_type = "LP_INTRODUCE_GOODS";
        document.importRequest = true;
        document.owner_inn = "15";
        document.participant_inn = "16";
        document.producer_inn = "23";
        document.production_date = "2020-01-23";
        document.production_type = "TYPE";
        document.reg_date = "2020-01-23";
        document.reg_number = "42";
        CrptApi.Document.Product product = new CrptApi.Document.Product();
        product.certificate_document = "doc";
        product.certificate_document_date = "2020-01-23";
        product.certificate_document_number = "1";
        product.owner_inn = "123";
        product.producer_inn = "24444666666";
        product.production_date = "2020-01-23";
        product.tnved_code = "-1";
        product.uit_code = "-2";
        product.uitu_code = "-3";
        document.products = new CrptApi.Document.Product[]{product};
    }

    // Проверка, что при переполнении не выбрасывается исключение
    @Test
    public void testCreateDocumentLimit() throws Exception {
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++){
                crptApi.createDocument(document, "signature");
            }
        });
    }

    // Проверка, что ресурсы освобождаются корректно: 55 запросов должны отправиться за 10 секунд.
    // Одна дополнительная секунда добавлена, т.к. команды выполняются не мгновенно.
    @Test
    public void testQueueClearing() throws Exception {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 55; i++){
            crptApi.createDocument(document, "signature");
        }
        long finishTime = System.currentTimeMillis();

        assertTrue((finishTime - startTime) < 11000, "Очередь очищается неверно");
    }

    // Проверка, что не превышен лимит запросов: 305 запросов должны отправиться не быстрее, чем за 60 секунд.
    @Test
    public void testTimeLimit() throws Exception {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 305; i++){
            crptApi.createDocument(document, "signature");
        }
        long finishTime = System.currentTimeMillis();

        assertTrue((finishTime - startTime) > 60000, "Превышен лимит запросов");
    }
}
