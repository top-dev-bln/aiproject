FROM fluent/fluentd:v1.16.3-1.0
USER root
RUN gem uninstall -I elasticsearch || true \
    && gem install elasticsearch -v 7.17.0 \
    && gem install fluent-plugin-elasticsearch --no-document --version 5.0.3