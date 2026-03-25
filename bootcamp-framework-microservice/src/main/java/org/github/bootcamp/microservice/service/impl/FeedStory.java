package org.github.bootcamp.microservice.service.impl;

import java.time.Instant;

record FeedStory(String title, String link, Instant publishedAt, String source) {}
