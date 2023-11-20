FROM python:3.11
EXPOSE 5000

RUN mkdir /leapme
WORKDIR /leapme

# Download glove embeddings and LEAPME precomputed features
RUN mkdir glove
WORKDIR glove
RUN curl -L --remote-name "https://huggingface.co/stanfordnlp/glove/resolve/main/glove.42B.300d.zip" && \
    unzip glove.42B.300d.zip && \
    split -l 1000 --suffix-length 5 --numeric-suffixes=10000 glove.42B.300d.txt glove.42B.300d && \
    rm glove.42B.300d.txt glove.42B.300d.zip
WORKDIR ..
RUN curl --remote-name "https://github.eii.us.es/raw/dayala1/LEAPME/412d318394496d13ecae893b34918514c705dbd0/features/final_features.zip" && \
    unzip final_features.zip && rm final_features.zip

# Install dependencies
RUN apt update && apt install -y tini && rm -rf /var/lib/apt/lists/*
RUN pip install poetry
COPY poetry.lock .
COPY pyproject.toml .
RUN poetry install
RUN poetry run python -c "import nltk; nltk.download('punkt')"

# Only copy remaining files now so that we can avoid having to reinstall dependencies every time some other file changes.
COPY . .

# Prepare glove index
RUN poetry run python glove_preprocessing.py glove

# Build model for LEAPME
RUN mkdir models
RUN poetry run python -u train_leapme_model.py

ENTRYPOINT ["tini", "--", "poetry", "run", "python", "leapme_server.py"]
