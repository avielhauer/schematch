FROM python:3.10
EXPOSE 5001

RUN mkdir /embdi
WORKDIR /embdi

# install embdi from github
RUN git clone https://gitlab.eurecom.fr/cappuzzo/embdi.git
WORKDIR embdi
RUN pip install .

# install other requirements
WORKDIR ..
COPY requirements .

RUN pip install -r requirements

COPY . .

ENTRYPOINT ["python", "embdi_server.py"]
