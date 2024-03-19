FROM python:3.10
EXPOSE 5001

RUN mkdir /embdi
WORKDIR /embdi

# install embdi from github
RUN git clone https://github.com/Tratori/embdi.git
WORKDIR embdi
RUN pip install .

# install other requirements
RUN apt update && apt install -y tini && rm -rf /var/lib/apt/lists/*
WORKDIR ..
COPY requirements .

RUN pip install -r requirements

COPY . .

ENTRYPOINT ["tini", "python", "embdi_server.py"]
