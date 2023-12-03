FROM python:2.7.18
EXPOSE 5002

RUN mkdir /adnev
WORKDIR /adnev

# install other requirements
RUN apt update && apt install -y tini && rm -rf /var/lib/apt/lists/*
WORKDIR ..
# for pyfm, install cython first
RUN pip install cython numpy
COPY requirements .

RUN pip install -r requirements

RUN git clone https://github.com/shraga89/DSMA.git
WORKDIR /DSMA

COPY . .

ENTRYPOINT ["tini", "python", "adnev_server.py"]
