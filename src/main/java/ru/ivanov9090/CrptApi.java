package ru.ivanov9090;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.*;

public class CrptApi {

    private final HttpClientImp httpClientImp;
    private final RequestLimiter requestLimiter;
    private final ObjectMapper objectMapper;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.httpClientImp = new HttpClientImp();
        this.requestLimiter = new RequestLimiter(timeUnit, requestLimit);
        this.objectMapper = new ObjectMapper();
    }

    public void createDocument(Document document, String signature) throws IOException, InterruptedException {
        // Узнаем, можно ли отправить запрос
        requestLimiter.acquire();

        // Преобразовываем в JSON
        String jsonDocument = objectMapper.writeValueAsString(document);

        // Отправляем запрос в отдельной реализации
        httpClientImp.post("https://ismp.crpt.ru/api/v3/lk/documents/create", jsonDocument, signature);

        // Освобождаем ресурс через необходимый период
        requestLimiter.release();
    }

    //Класс, ограничивающий количество запросов в единицу времени
    private static class RequestLimiter {
        private final long period;
        private final Semaphore semaphore;
        private final ScheduledExecutorService scheduledExecutorService;

        public RequestLimiter(TimeUnit timeUnit, int requestLimit) {
            this.semaphore = new Semaphore(requestLimit);
            this.scheduledExecutorService = Executors.newScheduledThreadPool(requestLimit);
            this.period = timeUnit.toMillis(1);
        }

        public void acquire() throws InterruptedException {
            semaphore.acquire(); // Можно расширить функционал при необходимости
        }
        public void release() throws InterruptedException {
            scheduledExecutorService.schedule(() -> {
                semaphore.release();
            }, period, TimeUnit.MILLISECONDS); // Через период возвращаем этот ресурс
        }
    }

    // Пример реализации отправки HTTP запроса.
    private static class HttpClientImp {
        public void post(String url, String jsonDocument, String signature) throws IOException {
             //Реализация HTTP POST запроса
             OkHttpClient client = new OkHttpClient();
             RequestBody body = RequestBody.create(jsonDocument, MediaType.get("application/json"));
             Request request = new Request.Builder()
                 .url(url)
                 .addHeader("Signature", signature)
                 .post(body)
                 .build();
             Response response = client.newCall(request).execute();
        }
    }

    public static class Document {
        // Некоторые поля фиксированы, но для возможности изменения, не будем фиксировать их
        public Description description; // Потенциально может состоять не только из 1 строки
        public String doc_id;
        public String doc_status;
        public String doc_type; // LP_INTRODUCE_GOODS, потенциально может поменяться
        public boolean importRequest; // true, потенциально может поменяться
        public String owner_inn;
        public String participant_inn;
        public String producer_inn;
        public String production_date; // 2020-01-23, потенциально может поменяться
        public String production_type;
        public Product[] products;
        public String reg_date; // 2020-01-23, потенциально может поменяться
        public String reg_number;

        public static class Description {
            public String participantInn;
        }

        public static class Product {
            public String certificate_document;
            public String certificate_document_date; // 2020-01-23, потенциально может поменяться
            public String certificate_document_number;
            public String owner_inn;
            public String producer_inn;
            public String production_date; // 2020-01-23, потенциально может поменяться
            public String tnved_code;
            public String uit_code;
            public String uitu_code;
        }
    }
}
