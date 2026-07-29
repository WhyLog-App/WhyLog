import type { GitCommitDetailFile } from "@/types/git";
import CommitChangedFile from "./CommitChangedFile";

interface Props {
  files: GitCommitDetailFile[];
}

const CommitFileList = ({ files }: Props) => {
  return (
    <div className="flex flex-col gap-4">
      {files.map((file) => (
        <CommitChangedFile key={file.fileName} file={file} />
      ))}
    </div>
  );
};

export default CommitFileList;
