FROM python:3.7
EXPOSE 5003

RUN mkdir /ic
WORKDIR /ic

RUN git clone https://github.com/wyy-code/NATool.git
WORKDIR NATool

# install requirements
RUN apt update && apt install -y tini && rm -rf /var/lib/apt/lists/*
COPY requirements .
RUN pip install -r requirements

COPY . .

ENTRYPOINT ["tini", "python", "ic_server.py"]
