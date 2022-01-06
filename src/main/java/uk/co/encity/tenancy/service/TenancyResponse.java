package uk.co.encity.tenancy.service;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import uk.co.encity.tenancy.entity.TenancyView;

@Getter
public class TenancyResponse {
    private String errorMessage;
    private TenancyView tenancyView;

    public TenancyResponse(TenancyView v) {
        this.errorMessage = null;
        this.tenancyView = v;
    }

    public TenancyResponse(String err, TenancyView v) {
        this(v);
        this.errorMessage = err;
    }
}
