FROM openjdk:16-jdk
RUN mkdir /app
COPY ./build/install/basura/ /app/
WORKDIR /app/bin
CMD ["./basura"]