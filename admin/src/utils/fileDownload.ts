function fileDownload(
  url: string,
  filename: string,
  requestInit?: RequestInit,
) {
  return fetch(url, requestInit)
    .then((res) => res.blob())
    .then((blob) => {
      // eslint-disable-next-line @typescript-eslint/no-shadow
      const url = window.URL || window.webkitURL;
      const link = url.createObjectURL(blob);
      const a = document.createElement('a');
      a.download = filename;
      a.id = link;
      a.href = link;
      document.body.append(a);
      a.click();
      window.URL.revokeObjectURL(link);
      const a2 = document.getElementById(link);
      a2?.remove();
    });
}

export default fileDownload;
