package org.oddjob.beanbus.pipeline;

interface Link<I, T> {

    <U> Pipeline.Stage<I, U> to(Section<? super T, U> section);

    <U> Pipeline.Stage<I ,U> to(Section<? super T, U> section, Pipeline.Options options);
}

