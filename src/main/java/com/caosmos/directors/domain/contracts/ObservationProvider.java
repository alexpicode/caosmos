package com.caosmos.directors.domain.contracts;

import com.caosmos.directors.domain.model.ObservationRequest;

public interface ObservationProvider {

  String observe(ObservationRequest request);
}
