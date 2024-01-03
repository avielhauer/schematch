FROM python:3.7
EXPOSE 5003

RUN mkdir /ic
WORKDIR /ic

RUN git clone https://github.com/wyy-code/GALib.git
RUN cd GALib && git checkout c263e3dc46d12993809c86d02f99f90a55b181ff && cd ..

# install requirements
RUN apt update && apt install -y tini && rm -rf /var/lib/apt/lists/*
COPY requirements .
RUN pip install -r requirements

COPY . .

ENTRYPOINT ["tini", "python", "ic_server.py"]
