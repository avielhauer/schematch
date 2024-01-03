FROM python:3.8
EXPOSE 5004

RUN mkdir /ic
WORKDIR /ic

# install requirements
RUN apt update && apt install -y tini && rm -rf /var/lib/apt/lists/*
COPY requirements .
RUN pip install -r requirements

COPY . .

ENTRYPOINT ["tini", "python", "ic_server.py"]
