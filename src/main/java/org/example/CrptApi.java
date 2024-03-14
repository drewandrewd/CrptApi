package main.java.org.example;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CrptApi {

    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final Object locker = new Object();
    private long lastResetTime = System.currentTimeMillis();
    //счетчик кол-во выполненных запросов после последнего сброс
    private int requestCount = 0;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
    }

    //создание документа
    public void createDoc(Document document, String signature) {
        synchronized (locker) {
            reset();
            while (requestCount >= requestLimit) {
                try {
                    locker.wait();
                    reset();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            requestCount++;
            sendRequest(document, signature);

        }
    }

    //проверка и сброс счетчика запросов
    public void reset() {
        long currentTime = System.currentTimeMillis();
        long interval = timeUnit.toMillis(1);
        if (currentTime - lastResetTime > interval) {
            requestCount = 0;
            lastResetTime = currentTime;
        }
    }

    //отправка запроса
    public void sendRequest(Document document, String signature) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(createRequestBody(document, signature)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            if (statusCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Успешно");
            } else {
                System.out.println("Ошибка");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String createRequestBody(Document document, String signature) {
        return "{\"description\": {\"participantInn\": \"" + document.getDescription().getParticipantInn() + "\"}, " +
                "\"doc_id\": \"" + document.getDocId() + "\", " +
                "\"doc_status\": \"" + document.getDocStatus() + "\", " +
                "\"doc_type\": \"" + document.getDocType() + "\", " +
                "\"importRequest\": " + document.isImportRequest() + ", " +
                "\"owner_inn\": \"" + document.getOwnerInn() + "\", " +
                "\"participant_inn\": \"" + document.getParticipantInn() + "\", " +
                "\"producer_inn\": \"" + document.getProducerInn() + "\", " +
                "\"production_date\": \"" + document.getProductionDate().toString() + "\", " +
                "\"production_type\": \"" + document.getProductionType() + "\", " +
                "\"products\": [{\"certificate_document\": \"" + document.getProducts().get(0).getCertificateDocument() + "\", " +
                "\"certificate_document_date\": \"" + document.getProducts().get(0).getCertificateDocumentDate().toString() + "\", " +
                "\"certificate_document_number\": \"" + document.getProducts().get(0).getCertificateDocumentNumber() + "\", " +
                "\"owner_inn\": \"" + document.getProducts().get(0).getOwnerInn() + "\", " +
                "\"producer_inn\": \"" + document.getProducts().get(0).getProducerInn() + "\", " +
                "\"production_date\": \"" + document.getProducts().get(0).getProductionDate().toString() + "\", " +
                "\"tnved_code\": \"" + document.getProducts().get(0).getTnvedCode() + "\", " +
                "\"uit_code\": \"" + document.getProducts().get(0).getUitCode() + "\", " +
                "\"uitu_code\": \"" + document.getProducts().get(0).getUituCode() + "\"}], " +
                "\"reg_date\": \"" + document.getRegDate().toString() + "\", " +
                "\"reg_number\": \"" + document.getRegNumber() + "\"}";
    }

    static class Document {

        private Description description;
        private String docId;
        private String docStatus;
        private String docType;
        private boolean importRequest;
        private String ownerInn;
        private String participantInn;
        private String producerInn;
        private Date productionDate;
        private String productionType;
        private List<Product> products;
        private Date regDate;
        private String regNumber;

        public Description getDescription() {
            return description;
        }

        public void setDescription(Description description) {
            this.description = description;
        }

        public String getDocId() {
            return docId;
        }

        public void setDocId(String docId) {
            this.docId = docId;
        }

        public String getDocType() {
            return docType;
        }

        public void setDocType(String docType) {
            this.docType = docType;
        }

        public String getDocStatus() {
            return docStatus;
        }

        public void setDocStatus(String docStatus) {
            this.docStatus = docStatus;
        }

        public boolean isImportRequest() {
            return importRequest;
        }

        public void setImportRequest(boolean importRequest) {
            this.importRequest = importRequest;
        }

        public String getOwnerInn() {
            return ownerInn;
        }

        public void setOwnerInn(String ownerInn) {
            this.ownerInn = ownerInn;
        }

        public String getParticipantInn() {
            return participantInn;
        }

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }

        public String getProducerInn() {
            return producerInn;
        }

        public void setProducerInn(String producerInn) {
            this.producerInn = producerInn;
        }

        public Date getProductionDate() {
            return productionDate;
        }

        public void setProductionDate(Date productionDate) {
            this.productionDate = productionDate;
        }

        public List<Product> getProducts() {
            return products;
        }

        public void setProducts(List<Product> products) {
            this.products = products;
        }

        public String getProductionType() {
            return productionType;
        }

        public void setProductionType(String productionType) {
            this.productionType = productionType;
        }

        public Date getRegDate() {
            return regDate;
        }

        public void setRegDate(Date regDate) {
            this.regDate = regDate;
        }

        public String getRegNumber() {
            return regNumber;
        }

        public void setRegNumber(String regNumber) {
            this.regNumber = regNumber;
        }

        static class Description {
            private String participantInn;

            public String getParticipantInn() {
                return participantInn;
            }

            public void setParticipantInn(String participantInn) {
                this.participantInn = participantInn;
            }
        }

        static class Product {
            private String certificateDocument;
            private Date certificateDocumentDate;
            private String certificateDocumentNumber;
            private String ownerInn;
            private String producerInn;
            private Date productionDate;
            private String tnvedCode;
            private String uitCode;
            private String uituCode;

            public String getCertificateDocument() {
                return certificateDocument;
            }

            public void setCertificateDocument(String certificateDocument) {
                this.certificateDocument = certificateDocument;
            }

            public Date getCertificateDocumentDate() {
                return certificateDocumentDate;
            }

            public void setCertificateDocumentDate(Date certificateDocumentDate) {
                this.certificateDocumentDate = certificateDocumentDate;
            }

            public String getCertificateDocumentNumber() {
                return certificateDocumentNumber;
            }

            public void setCertificateDocumentNumber(String certificateDocumentNumber) {
                this.certificateDocumentNumber = certificateDocumentNumber;
            }

            public String getUituCode() {
                return uituCode;
            }

            public void setUituCode(String uituCode) {
                this.uituCode = uituCode;
            }

            public String getUitCode() {
                return uitCode;
            }

            public void setUitCode(String uitCode) {
                this.uitCode = uitCode;
            }

            public String getTnvedCode() {
                return tnvedCode;
            }

            public void setTnvedCode(String tnvedCode) {
                this.tnvedCode = tnvedCode;
            }

            public Date getProductionDate() {
                return productionDate;
            }

            public void setProductionDate(Date productionDate) {
                this.productionDate = productionDate;
            }

            public String getProducerInn() {
                return producerInn;
            }

            public void setProducerInn(String producerInn) {
                this.producerInn = producerInn;
            }

            public String getOwnerInn() {
                return ownerInn;
            }

            public void setOwnerInn(String ownerInn) {
                this.ownerInn = ownerInn;
            }
        }
    }
}
