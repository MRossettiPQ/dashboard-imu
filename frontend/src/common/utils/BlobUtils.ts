declare global {
  interface Navigator {
    msSaveBlob: (blob: Blob, fileName: string) => boolean;
  }
}

export class BlobDownloader {
  download(
    content: string | BlobPart[] | Blob | undefined,
    filename = 'filename',
    options?: BlobPropertyBag,
  ): void {
    if (!content) {
      return;
    }

    let blob = null;
    if (typeof content === 'string') {
      blob = new Blob([content], options);
    } else if (Array.isArray(content)) {
      blob = new Blob(content, options);
    } else {
      blob = content;
    }

    if (navigator.msSaveBlob) {
      // Para o Internet Explorer
      navigator.msSaveBlob(blob, filename);
    } else {
      // Para outros navegadores
      const link = document.createElement('a');
      if (link.download != undefined) {
        // Gerar link para download do arquivo no navegador
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', filename);
        link.style.visibility = 'hidden';

        document.body.appendChild(link);
        link.click();

        // Remover link de download
        document.body.removeChild(link);
      }
    }
  }
}
