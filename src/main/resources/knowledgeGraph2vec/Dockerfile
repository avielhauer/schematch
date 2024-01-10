FROM python:3.11
EXPOSE 5004

RUN mkdir /2vec
WORKDIR /2vec

# Install dependencies
RUN apt update && apt install -y tini && rm -rf /var/lib/apt/lists/*

COPY requirements .
RUN pip install -r requirements
# Only copy remaining files now so that we can avoid having to reinstall dependencies every time some other file changes.
COPY . .

ENTRYPOINT ["tini", "--", "poetry", "run", "python", "2vec_server.py"]
