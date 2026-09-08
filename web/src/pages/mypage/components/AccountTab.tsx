import type { MyInfo } from "@/types/member";
import { PasswordChangeSection } from "./PasswordChangeSection";
import { ProfileEditSection } from "./ProfileEditSection";
import { WithdrawalSection } from "./WithdrawalSection";

interface AccountTabProps {
  info: MyInfo;
}

const accountInputClassName =
  "typo-body5 h-11 w-full min-w-0 rounded-xl border border-white bg-white/50 px-4 text-(--color-text-primary) placeholder:text-(--color-text-tertiary) focus:outline-none focus:ring-1 focus:ring-(--color-border-brand)";

export const AccountTab = ({ info }: AccountTabProps) => {
  return (
    <div className="grid gap-8">
      <div className="grid items-stretch gap-8 lg:grid-cols-2">
        <ProfileEditSection
          info={info}
          inputClassName={accountInputClassName}
        />
        <PasswordChangeSection inputClassName={accountInputClassName} />
      </div>

      <WithdrawalSection />
    </div>
  );
};
