package sat301.s_martproject.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import sat301.s_martproject.model.CurrencyResponse;

@Service
public class CurrencyConversionService {

    private static final String API_URL = "https://v6.exchangerate-api.com/v6/c57f794edd6ae610a975ce5d/latest/CNY"; // Use CNY as the base currency

    public double convertCurrency(String toCurrency, double amountInCNY) {
        RestTemplate restTemplate = new RestTemplate();
        CurrencyResponse response = restTemplate.getForObject(API_URL, CurrencyResponse.class);

        if (response != null && response.getConversion_rates().containsKey(toCurrency)) {
            double rate = response.getConversion_rates().get(toCurrency);
            return Math.round(amountInCNY * rate * 100.0) / 100.0;
        }
        return amountInCNY; // fallback: no conversion
    }
}
