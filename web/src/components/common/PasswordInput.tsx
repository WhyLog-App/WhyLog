import type { ChangeEvent, InputHTMLAttributes } from "react";
import { useState } from "react";
import IconEye from "@/assets/icons/interface/ic_eye.svg?react";
import IconEyeOff from "@/assets/icons/interface/ic_eye_off.svg?react";
import { Icon } from "@/components/common/Icon";

interface PasswordInputProps {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  autoComplete: InputHTMLAttributes<HTMLInputElement>["autoComplete"];
  labelClassName?: string;
  inputClassName: string;
  disabled?: boolean;
  maxLength?: number;
}

export const PasswordInput = ({
  id,
  label,
  value,
  onChange,
  placeholder,
  autoComplete,
  labelClassName = "typo-label text-(--color-text-secondary)",
  inputClassName,
  disabled = false,
  maxLength,
}: PasswordInputProps) => {
  const [isVisible, setIsVisible] = useState(false);
  const inputType = isVisible ? "text" : "password";

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    onChange(event.target.value);
  };

  return (
    <div className="flex flex-col gap-1">
      <label htmlFor={id} className={labelClassName}>
        {label}
      </label>
      <div className="relative">
        <input
          id={id}
          type={inputType}
          value={value}
          onChange={handleChange}
          placeholder={placeholder}
          autoComplete={autoComplete}
          disabled={disabled}
          maxLength={maxLength}
          className={`${inputClassName} pr-12`}
        />
        <button
          type="button"
          onClick={() => setIsVisible((current) => !current)}
          className="absolute right-3 top-1/2 flex -translate-y-1/2 cursor-pointer items-center justify-center rounded-full p-1 text-(--color-text-tertiary) hover:text-(--color-text-secondary) focus:outline-none focus:ring-1 focus:ring-white disabled:cursor-not-allowed disabled:opacity-50"
          aria-label={isVisible ? "비밀번호 숨기기" : "비밀번호 보기"}
          aria-pressed={isVisible}
          disabled={disabled}
        >
          <Icon icon={isVisible ? IconEyeOff : IconEye} size={18} />
        </button>
      </div>
    </div>
  );
};
